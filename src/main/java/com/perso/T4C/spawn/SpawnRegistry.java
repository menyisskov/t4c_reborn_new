package com.perso.T4C.spawn;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class SpawnRegistry {
  private static volatile List<SpawnDefinition> cachedNpcs;
  private static volatile List<SpawnDefinition> cachedMonsters;

  private SpawnRegistry() {}

  public static List<SpawnDefinition> npcs() {
    List<SpawnDefinition> cached = cachedNpcs;
    if (cached != null) return cached;
    synchronized (SpawnRegistry.class) {
      if (cachedNpcs == null) {
        cachedNpcs = buildNpcs();
      }
      return cachedNpcs;
    }
  }

  private static List<SpawnDefinition> buildNpcs() {
    List<SpawnDefinition> result = new ArrayList<>();
    result.addAll(scan("com.perso.T4C.npc", Set.of(SpawnKind.AUTO, SpawnKind.NPC)));
    return unique(result);
  }

  public static List<SpawnDefinition> monsters() {
    List<SpawnDefinition> cached = cachedMonsters;
    if (cached != null) return cached;
    synchronized (SpawnRegistry.class) {
      if (cachedMonsters == null) {
        cachedMonsters = buildMonsters();
      }
      return cachedMonsters;
    }
  }

  private static List<SpawnDefinition> buildMonsters() {
    List<SpawnDefinition> result = new ArrayList<>();
    result.addAll(scan("com.perso.T4C.monster", Set.of(SpawnKind.AUTO, SpawnKind.MONSTER)));
    result.addAll(scan("com.perso.T4C.npc", Set.of(SpawnKind.MONSTER)));
    return unique(result);
  }

  public static List<SpawnDefinition> all(String packageName) {
    String packagePath = packageName.replace('.', '/');
    Set<String> classNames = new TreeSet<>();
    try {
      Enumeration<URL> resources = loader().getResources(packagePath);
      while (resources.hasMoreElements())
        collect(resources.nextElement(), packageName, packagePath, classNames);
      collectClasspathEntries(packageName, packagePath, classNames);
      List<SpawnDefinition> result = new ArrayList<>();
      for (String className : classNames)
        add(className, result, Set.of(SpawnKind.AUTO, SpawnKind.NPC, SpawnKind.MONSTER));
      result.sort(
          Comparator.comparing(SpawnDefinition::type, Comparator.nullsFirst(String::compareTo))
              .thenComparingInt(SpawnDefinition::x)
              .thenComparingInt(SpawnDefinition::y)
              .thenComparingInt(SpawnDefinition::z)
              .thenComparing(SpawnDefinition::stationary)
              .thenComparing(SpawnDefinition::aggressive));
      validateDuplicates(result, packageName);
      return List.copyOf(result);
    } catch (IOException e) {
      throw new IllegalStateException("Unable to scan spawn package " + packageName, e);
    }
  }

  private static List<SpawnDefinition> scan(String packageName, Set<SpawnKind> kinds) {
    String packagePath = packageName.replace('.', '/');
    Set<String> classNames = new TreeSet<>();
    try {
      Enumeration<URL> resources = loader().getResources(packagePath);
      while (resources.hasMoreElements())
        collect(resources.nextElement(), packageName, packagePath, classNames);
      collectClasspathEntries(packageName, packagePath, classNames);
      List<SpawnDefinition> result = new ArrayList<>();
      for (String className : classNames) add(className, result, kinds);
      return result;
    } catch (IOException e) {
      throw new IllegalStateException("Unable to scan spawn package " + packageName, e);
    }
  }

  private static List<SpawnDefinition> unique(List<SpawnDefinition> definitions) {
    Set<SpawnDefinition> unique = new LinkedHashSet<>(definitions);
    return List.copyOf(unique);
  }

  private static void collect(
      URL resource, String packageName, String packagePath, Set<String> names) {
    try {
      if ("file".equals(resource.getProtocol()))
        scanDirectory(new File(resource.toURI()), packageName, names);
      else if ("jar".equals(resource.getProtocol())) {
        JarURLConnection connection = (JarURLConnection) resource.openConnection();
        scanJar(connection.getJarFile(), packagePath, names);
      }
    } catch (Exception e) {
      throw new IllegalStateException("Unable to scan " + resource, e);
    }
  }

  private static void collectClasspathEntries(
      String packageName, String packagePath, Set<String> names) throws IOException {
    for (String entry :
        System.getProperty("java.class.path", "")
            .split(java.util.regex.Pattern.quote(File.pathSeparator))) {
      if (entry.isBlank()) continue;
      File file = new File(entry);
      if (file.isDirectory()) {
        File directory = new File(file, packagePath);
        if (directory.isDirectory()) scanDirectory(directory, packageName, names);
      } else if (file.isFile() && entry.toLowerCase().endsWith(".jar")) {
        try (JarFile jar = new JarFile(file)) {
          scanJar(jar, packagePath, names);
        }
      }
    }
  }

  private static void scanJar(JarFile jar, String packagePath, Set<String> names) {
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(packagePath + "/") && name.endsWith(".class") && !name.contains("$"))
        names.add(name.substring(0, name.length() - 6).replace('/', '.'));
    }
  }

  private static void scanDirectory(File directory, String packageName, Set<String> names) {
    File[] files = directory.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory()) scanDirectory(file, packageName + "." + file.getName(), names);
      else if (file.getName().endsWith(".class") && !file.getName().contains("$"))
        names.add(packageName + "." + file.getName().substring(0, file.getName().length() - 6));
    }
  }

  private static void add(String className, List<SpawnDefinition> result, Set<SpawnKind> kinds) {
    try {
      Class<?> type = Class.forName(className, false, loader());
      for (Spawn spawn : type.getAnnotationsByType(Spawn.class))
        if (kinds.contains(spawn.kind()))
          result.add(
              new SpawnDefinition(
                  spawn.type(),
                  spawn.x(),
                  spawn.y(),
                  spawn.z(),
                  spawn.stationary(),
                  spawn.aggressive()));
    } catch (ClassNotFoundException ignored) {
    }
  }

  private static void validateDuplicates(List<SpawnDefinition> definitions, String packageName) {
    Set<SpawnDefinition> unique = new LinkedHashSet<>();
    for (SpawnDefinition definition : definitions)
      if (!unique.add(definition))
        throw new IllegalStateException(
            "Duplicate spawn definition in " + packageName + ": " + definition);
  }

  private static ClassLoader loader() {
    return SpawnRegistry.class.getClassLoader();
  }
}
