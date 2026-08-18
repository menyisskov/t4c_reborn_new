package com.perso.T4C.npc.generated;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.npc.registry.NpcContext;
import com.perso.T4C.npc.registry.NpcFactoryRegistry;
import com.perso.T4C.npc.registry.NpcSpec;
import java.io.File;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Supplier;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public final class GeneratedNpcIndex {
  private static final String ROOT = "com.perso.T4C.npc";

  private GeneratedNpcIndex() {}

  public static List<NpcFactoryRegistry.Registration> registrations() {
    List<NpcFactoryRegistry.Registration> result = new ArrayList<>();
    try {
      Enumeration<URL> resources =
          GeneratedNpcIndex.class.getClassLoader().getResources(ROOT.replace('.', '/'));
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if ("file".equals(resource.getProtocol())) {
          scanDirectory(new File(resource.toURI()), ROOT, result);
        } else if ("jar".equals(resource.getProtocol())) {
          JarURLConnection connection = (JarURLConnection) resource.openConnection();
          scanJar(connection.getJarFile(), ROOT.replace('.', '/'), result);
        }
      }
      return result;
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static void scanDirectory(
      File directory, String packageName, List<NpcFactoryRegistry.Registration> result) {
    File[] files = directory.listFiles();
    if (files == null) return;
    for (File file : files) {
      if (file.isDirectory()) scanDirectory(file, packageName + "." + file.getName(), result);
      else if (file.getName().endsWith(".class") && !file.getName().contains("$")) {
        addClass(packageName + "." + file.getName().replaceFirst("\\.class$", ""), result);
      }
    }
  }

  private static void scanJar(
      JarFile jar, String packagePath, List<NpcFactoryRegistry.Registration> result) {
    Enumeration<JarEntry> entries = jar.entries();
    while (entries.hasMoreElements()) {
      String name = entries.nextElement().getName();
      if (name.startsWith(packagePath + "/") && name.endsWith(".class") && !name.contains("$")) {
        addClass(name.substring(0, name.length() - 6).replace('/', '.'), result);
      }
    }
  }

  private static void addClass(String className, List<NpcFactoryRegistry.Registration> result) {
    try {
      Class<?> type = Class.forName(className, true, GeneratedNpcIndex.class.getClassLoader());
      var idField = type.getDeclaredField("ID");
      idField.setAccessible(true);
      String id = (String) idField.get(null);
      String displayName = readString(type, "DISPLAY_NAME", "${npc." + id.toLowerCase() + "}");
      String spriteBase = readString(type, "SPRITE_BASE", type.getSimpleName());
      if ("Sabrina".equals(id)) return;
      var constructor = type.getDeclaredConstructor(NpcContext.class);
      constructor.setAccessible(true);
      result.add(
          new NpcFactoryRegistry.Registration(
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
              findSpecification(type)));
    } catch (NoSuchFieldException | NoSuchMethodException ignored) {
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Invalid NPC class: " + className, e);
    }
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
}
