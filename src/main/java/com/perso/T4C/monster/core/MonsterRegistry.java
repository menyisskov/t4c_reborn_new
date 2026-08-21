package com.perso.T4C.monster.core;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.i18n.I18n;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;
import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class MonsterRegistry {
  @FunctionalInterface
  public interface MonsterFactory {
    BaseMonster create(MonsterDef definition, float worldX, float worldY) throws GameException;
  }

  private static final Map<String, MonsterFactory> SPECIALIZED_FACTORIES = new LinkedHashMap<>();
  private static final Map<String, MonsterDef> SPAWN_TYPE_ALIASES = new LinkedHashMap<>();
  private static List<MonsterDef> cache;
  private static Map<String, MonsterDef> byName;
  private static Map<String, MonsterDef> byNormalizedName;
  private static Map<String, MonsterDef> byAlias;
  private static List<MonsterDef> javaDefinitions = List.of();

  private MonsterRegistry() {}

  static {
    registerGeneratedFactories();
  }

  public static synchronized void registerSpecialized(String name, MonsterFactory factory) {
    if (name == null || name.isBlank() || factory == null) {
      throw new IllegalArgumentException("Monster specialization requires a name and factory");
    }
    SPECIALIZED_FACTORIES.put(normalize(name), factory);
  }

  public static synchronized void registerDefinitions(List<MonsterDef> definitions) {
    javaDefinitions = definitions == null ? List.of() : List.copyOf(definitions);
    cache = null;
    byName = null;
    byNormalizedName = null;
    byAlias = null;
  }

  private static void registerGeneratedFactories() {
    String packageName = MonsterRegistry.class.getPackageName().replace(".core", "");
    String packagePath = packageName.replace('.', '/');
    List<MonsterDef> definitions = new java.util.ArrayList<>();
    try {
      Enumeration<URL> resources = MonsterRegistry.class.getClassLoader().getResources(packagePath);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("file".equals(resource.getProtocol())) {
          File directory = new File(resource.toURI());
          File[] classes =
              directory.listFiles((dir, name) -> name.endsWith(".class") && !name.contains("$"));
          if (classes != null) {
            for (File file : classes)
              registerGeneratedClass(
                  packageName + '.' + file.getName().replace(".class", ""), definitions);
          }
        } else if ("jar".equals(resource.getProtocol())) {
          JarURLConnection connection = (JarURLConnection) resource.openConnection();
          try (JarFile jar = connection.getJarFile()) {
            Enumeration<JarEntry> entries = jar.entries();
            while (entries.hasMoreElements()) {
              String name = entries.nextElement().getName();
              if (name.startsWith(packagePath + "/")
                  && name.endsWith(".class")
                  && !name.contains("$")) {
                registerGeneratedClass(
                    name.substring(0, name.length() - 6).replace('/', '.'), definitions);
              }
            }
          }
        }
      }
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
    registerDefinitions(definitions);
  }

  private static void registerGeneratedClass(String className, List<MonsterDef> definitions) {
    try {
      Class<?> type = Class.forName(className, true, MonsterRegistry.class.getClassLoader());
      MonsterDef definition = (MonsterDef) type.getMethod("definition").invoke(null);
      definitions.add(definition);
      indexIdentity(type.getSimpleName(), definition);
      indexSpawnTypes(type, definition);
      var constructor = type.getConstructor(MonsterDef.class, float.class, float.class);
      registerSpecialized(
          definition.getName(),
          (def, x, y) -> {
            try {
              return (BaseMonster) constructor.newInstance(def, x, y);
            } catch (ReflectiveOperationException e) {
              throw new GameException("Unable to instantiate " + className, e);
            }
          });
      for (String alias : definition.getSpawnAliases()) {
        if (alias != null && !alias.isBlank()) {
          registerSpecialized(
              alias,
              (def, x, y) -> {
                try {
                  return (BaseMonster) constructor.newInstance(def, x, y);
                } catch (ReflectiveOperationException e) {
                  throw new GameException("Unable to instantiate " + className, e);
                }
              });
        }
      }
    } catch (NoSuchMethodException e) {
      return;
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Invalid generated monster class: " + className, e);
    }
  }

  public static synchronized BaseMonster create(MonsterDef definition, float worldX, float worldY)
      throws GameException {
    if (definition == null) throw new IllegalArgumentException("definition");
    MonsterFactory factory = SPECIALIZED_FACTORIES.get(normalize(definition.getName()));
    return factory == null
        ? new NamedEventMonster(definition, worldX, worldY)
        : factory.create(definition, worldX, worldY);
  }

  public static synchronized List<MonsterDef> load() {
    if (cache != null) {
      return cache;
    }
    rebuild(javaDefinitions);
    return cache;
  }

  public static synchronized void save(List<MonsterDef> defs) throws IOException {
    throw new UnsupportedOperationException(
        "Monster definitions are owned by Java monster classes");
  }

  public static synchronized MonsterDef findByName(String name) {
    load();
    if (name == null) return null;
    return lookup(name);
  }

  private static MonsterDef lookup(String name) {
    MonsterDef exact = byName.get(name);
    if (exact != null) {
      return exact;
    }
    String key = normalize(name);
    MonsterDef normalized = byNormalizedName.get(key);
    if (normalized != null) {
      return normalized;
    }
    MonsterDef alias = byAlias.get(name);
    return alias != null ? alias : byAlias.get(key);
  }

  private static void indexIdentity(String identity, MonsterDef definition) {
    if (identity == null || identity.isBlank()) {
      return;
    }
    SPAWN_TYPE_ALIASES.putIfAbsent(identity, definition);
    SPAWN_TYPE_ALIASES.putIfAbsent(normalize(identity), definition);
  }

  private static void indexSpawnTypes(Class<?> type, MonsterDef definition) {
    for (Spawn spawn : type.getAnnotationsByType(Spawn.class)) {
      String spawnType = spawn.type();
      if (spawnType == null || spawnType.isBlank()) {
        continue;
      }
      indexIdentity(spawnType, definition);
      registerSpecialized(
          spawnType,
          (def, x, y) -> {
            try {
              var constructor = type.getConstructor(MonsterDef.class, float.class, float.class);
              return (BaseMonster) constructor.newInstance(def, x, y);
            } catch (ReflectiveOperationException e) {
              throw new GameException("Unable to instantiate " + type.getName(), e);
            }
          });
    }
  }

  public static synchronized List<String> names() {
    load();
    return cache.stream()
        .filter(def -> def != null && def.getName() != null)
        .map(MonsterDef::getName)
        .toList();
  }

  public static synchronized void invalidate() {
    cache = null;
    byName = null;
    byNormalizedName = null;
  }

  private static void rebuild(List<MonsterDef> defs) {
    cache = List.copyOf(defs);
    Map<String, MonsterDef> map = new LinkedHashMap<>();
    Map<String, MonsterDef> normalized = new LinkedHashMap<>();
    Map<String, MonsterDef> aliases = new LinkedHashMap<>();
    for (MonsterDef def : cache) {
      if (def != null && def.getName() != null) {
        map.put(def.getName(), def);
        normalized.putIfAbsent(normalize(def.getName()), def);
        String displayKey = I18n.keyOf(def.getDisplayName());
        if (displayKey != null) {
          normalized.putIfAbsent(normalize(displayKey.substring(displayKey.indexOf('.') + 1)), def);
        } else if (def.getDisplayName() != null) {
          normalized.putIfAbsent(normalize(def.getDisplayName()), def);
        }
        if (def.getSpawnAliases() != null) {
          for (String alias : def.getSpawnAliases()) {
            if (alias != null && !alias.isBlank()) {
              aliases.putIfAbsent(alias, def);
              aliases.putIfAbsent(normalize(alias), def);
            }
          }
        }
      }
    }
    for (Map.Entry<String, MonsterDef> extra : SPAWN_TYPE_ALIASES.entrySet()) {
      aliases.putIfAbsent(extra.getKey(), extra.getValue());
    }
    byName = map;
    byNormalizedName = normalized;
    byAlias = aliases;
  }

  private static String normalize(String value) {
    return value.replaceAll("[^A-Za-z0-9]", "").toLowerCase(Locale.ROOT);
  }
}
