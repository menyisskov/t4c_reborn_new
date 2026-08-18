package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class SpellDefinitions {
  private static final String PACKAGE_NAME = "com.perso.T4C.spell.definition";
  private static final String PACKAGE_PATH = PACKAGE_NAME.replace('.', '/');

  private SpellDefinitions() {}

  public static List<SpellData> all() {
    List<SpellData> definitions = new ArrayList<>();
    for (String className : findClassNames()) {
      try {
        Class<?> type = Class.forName(className);
        if (type == SpellDefinitions.class || type.isInterface() || type.isEnum()) continue;
        Method definition = type.getDeclaredMethod("definition");
        if (!Modifier.isStatic(definition.getModifiers()) || definition.getParameterCount() != 0
            || !SpellData.class.isAssignableFrom(definition.getReturnType())) continue;
        SpellData spell = (SpellData) definition.invoke(null);
        if (spell != null) definitions.add(spell);
      } catch (ReflectiveOperationException | RuntimeException ignored) {
      }
    }
    definitions.sort(Comparator.comparingInt(SpellData::getSpellId));
    return List.copyOf(definitions);
  }

  private static Set<String> findClassNames() {
    Set<String> classNames = new LinkedHashSet<>();
    try {
      Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources(PACKAGE_PATH);
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("file".equals(resource.getProtocol())) {
          scanDirectory(Path.of(URI.create(resource.toString())), classNames);
        } else if ("jar".equals(resource.getProtocol())) {
          scanJar(((JarURLConnection) resource.openConnection()).getJarFile(), classNames);
        }
      }
    } catch (IOException | RuntimeException ignored) {
    }
    return classNames;
  }

  private static void scanDirectory(Path directory, Set<String> classNames) throws IOException {
    try (var files = Files.list(directory)) {
      files.filter(path -> path.getFileName().toString().endsWith(".class"))
          .map(path -> path.getFileName().toString().replaceFirst("\\.class$", ""))
          .filter(name -> !name.contains("$"))
          .sorted()
          .forEach(name -> classNames.add(PACKAGE_NAME + "." + name));
    }
  }

  private static void scanJar(JarFile jar, Set<String> classNames) {
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(PACKAGE_PATH + "/") && name.endsWith(".class") && !name.contains("$"))
        classNames.add(name.substring(0, name.length() - 6).replace('/', '.'));
    }
  }
}
