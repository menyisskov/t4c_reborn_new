package com.perso.T4C.npc.core;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.spawn.Spawn;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class NpcFactoryRegistry {

  private static final String ROOT = "com.perso.T4C.npc";
  private static final Map<String, Registration> BY_ID = new LinkedHashMap<>();
  private static final Map<String, Registration> BY_ALIAS = new LinkedHashMap<>();

  static {
    scanClasspath();
  }

  private NpcFactoryRegistry() {}

  public static BaseNPC create(String id, NpcContext context) throws GameException {

    Registration registration = find(id);

    return registration == null ? null : registration.factory().create(context);
  }

  public static Registration find(String id) {
    if (id == null) return null;
    String key = normalize(id);
    Registration registration = BY_ID.get(key);
    return registration != null ? registration : BY_ALIAS.get(key);
  }

  public static List<Registration> registrations() {

    return List.copyOf(BY_ID.values());
  }

  public static NpcSpec specification(String id) {

    Registration registration = find(id);

    return registration == null || registration.specification() == null
        ? null
        : registration.specification().get();
  }

  private static void register(Registration registration) {

    if (registration.specification() == null) {

      try {

        BaseNPC npc = registration.factory().create(new NpcContext(null));

        if (npc instanceof ScriptedNpc scripted) {

          registration =
              new Registration(
                  registration.id(),
                  registration.displayName(),
                  registration.spriteBase(),
                  registration.factory(),
                  scripted::specification,
                  registration.identities());
        }

      } catch (Exception ignored) {

      }
    }

    String key = normalize(registration.id());

    if (BY_ID.putIfAbsent(key, registration) != null) {

      throw new IllegalStateException("Duplicate NPC id: " + registration.id());
    }

    if (registration.identities() != null) {
      for (String identity : registration.identities()) {
        if (identity == null || identity.isBlank()) {
          continue;
        }
        String alias = normalize(identity);
        if (!alias.equals(key)) {
          BY_ALIAS.putIfAbsent(alias, registration);
        }
      }
    }
  }

  private static void scanClasspath() {
    try {
      Enumeration<URL> resources =
          NpcFactoryRegistry.class.getClassLoader().getResources(ROOT.replace('.', '/'));
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("file".equals(resource.getProtocol())) {
          scanDirectory(new File(resource.toURI()), ROOT);
        } else if ("jar".equals(resource.getProtocol())) {
          JarURLConnection connection = (JarURLConnection) resource.openConnection();
          scanJar(connection.getJarFile(), ROOT.replace('.', '/'));
        }
      }
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static void scanDirectory(File directory, String packageName) {
    File[] files = directory.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory()) scanDirectory(file, packageName + "." + file.getName());
      else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
        addClass(packageName + "." + file.getName().replaceFirst("\\.class$", ""));
      }
    }
  }

  private static void scanJar(JarFile jar, String packagePath) {
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(packagePath + "/") && name.endsWith(".class") && !name.contains("$")) {
        addClass(name.substring(0, name.length() - 6).replace('/', '.'));
      }
    }
  }

  private static void addClass(String className) {
    try {
      Class<?> type = Class.forName(className, true, NpcFactoryRegistry.class.getClassLoader());
      var idField = type.getDeclaredField("ID");
      idField.setAccessible(true);
      String id = (String) idField.get(null);
      String displayName = readString(type, "DISPLAY_NAME", "${npc." + id.toLowerCase() + "}");
      String spriteBase = readString(type, "SPRITE_BASE", type.getSimpleName());
      if ("Sabrina".equals(id)) return;
      var constructor = type.getDeclaredConstructor(NpcContext.class);
      constructor.setAccessible(true);
      register(
          new Registration(
              id,
              displayName,
              spriteBase,
              context -> {
                try {
                  return (BaseNPC) constructor.newInstance(context);
                } catch (ReflectiveOperationException e) {
                  throw new GameException("Unable to create NPC: " + className, e);
                }
              },
              findSpecification(type),
              identitiesOf(type)));
    } catch (NoSuchFieldException | NoSuchMethodException ignored) {
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Invalid NPC class: " + className, e);
    }
  }

  private static List<String> identitiesOf(Class<?> type) {
    List<String> identities = new ArrayList<>();
    identities.add(type.getSimpleName());
    for (Spawn spawn : type.getAnnotationsByType(Spawn.class)) {
      if (spawn.type() != null && !spawn.type().isBlank()) {
        identities.add(spawn.type());
      }
    }
    return identities;
  }

  private static String readString(Class<?> type, String name, String fallback)
      throws ReflectiveOperationException {
    try {
      var field = type.getDeclaredField(name);
      field.setAccessible(true);
      return (String) field.get(null);
    } catch (NoSuchFieldException e) {
      return fallback;
    }
  }

  private static Supplier<NpcSpec> findSpecification(Class<?> type) {
    try {
      Method method = type.getMethod("spec");
      if (!Modifier.isStatic(method.getModifiers())) return null;
      return () -> {
        try {
          return (NpcSpec) method.invoke(null);
        } catch (ReflectiveOperationException e) {
          throw new GameException("Unable to load NPC specification: " + type.getName(), e);
        }
      };
    } catch (NoSuchMethodException e) {
      return null;
    }
  }

  private static String normalize(String value) {

    return value.trim().toLowerCase(Locale.ROOT);
  }

  public record Registration(
      String id,
      String displayName,
      String spriteBase,
      Factory factory,
      Supplier<NpcSpec> specification,
      List<String> identities) {

    public Registration(String id, String displayName, String spriteBase, Factory factory) {

      this(id, displayName, spriteBase, factory, null, List.of());
    }

    public Registration(
        String id,
        String displayName,
        String spriteBase,
        Factory factory,
        Supplier<NpcSpec> specification) {

      this(id, displayName, spriteBase, factory, specification, List.of());
    }

    public Registration {
      if (id == null || id.isBlank()) throw new IllegalArgumentException("NPC id is required");
      if (factory == null) throw new IllegalArgumentException("NPC factory is required");
      identities = identities == null ? List.of() : List.copyOf(identities);
    }
  }

  @FunctionalInterface
  public interface Factory {

    BaseNPC create(NpcContext context) throws GameException;
  }
}
