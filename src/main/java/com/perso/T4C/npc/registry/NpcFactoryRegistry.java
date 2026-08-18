package com.perso.T4C.npc.registry;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.core.*;
import com.perso.T4C.npc.generated.GeneratedNpcIndex;
import com.perso.T4C.npc.script.*;
import java.io.File;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class NpcFactoryRegistry {

  private static final Map<String, Registration> BY_ID = new LinkedHashMap<>();

  static {
    GeneratedNpcIndex.registrations().forEach(NpcFactoryRegistry::register);
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
    if (registration == null && key.startsWith("mob")) {
      registration = BY_ID.get(key.substring(3));
    }
    return registration;
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
                  scripted::specification);
        }

      } catch (Exception ignored) {

      }
    }

    String key = normalize(registration.id());

    if (BY_ID.putIfAbsent(key, registration) != null) {

      throw new IllegalStateException("Duplicate NPC id: " + registration.id());
    }
  }

  private static void registerReflectiveNpcs() {
    try {
      Enumeration<URL> resources =
          NpcFactoryRegistry.class.getClassLoader().getResources("com/perso/T4C/npc/arakas");
      while (resources.hasMoreElements()) {
        URL resource = resources.nextElement();
        if (!"file".equals(resource.getProtocol())) continue;
        File directory = new File(resource.toURI());
        File[] files =
            directory.listFiles((dir, name) -> name.endsWith(".class") && !name.contains("$"));
        if (files == null) continue;
        for (File file : files)
          registerReflectiveNpc("com.perso.T4C.npc.arakas." + file.getName().replace(".class", ""));
      }
    } catch (Exception e) {
      throw new ExceptionInInitializerError(e);
    }
  }

  private static void registerReflectiveNpc(String className) {
    try {
      Class<?> type = Class.forName(className, true, NpcFactoryRegistry.class.getClassLoader());
      var id = type.getField("ID").get(null);
      var displayName = type.getField("DISPLAY_NAME").get(null);
      var spriteBase = type.getField("SPRITE_BASE").get(null);
      var constructor = type.getConstructor(NpcContext.class);
      if (BY_ID.containsKey(normalize((String) id))) return;
      register(
          new Registration(
              (String) id,
              (String) displayName,
              (String) spriteBase,
              context -> {
                try {
                  return (BaseNPC) constructor.newInstance(context);
                } catch (ReflectiveOperationException e) {
                  throw new GameException("Unable to create NPC: " + className, e);
                }
              }));
    } catch (NoSuchFieldException | NoSuchMethodException ignored) {
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Invalid NPC class: " + className, e);
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
      Supplier<NpcSpec> specification) {

    public Registration(String id, String displayName, String spriteBase, Factory factory) {

      this(id, displayName, spriteBase, factory, null);
    }

    public Registration {

      if (id == null || id.isBlank()) throw new IllegalArgumentException("NPC id is required");

      if (factory == null) throw new IllegalArgumentException("NPC factory is required");
    }
  }

  @FunctionalInterface
  public interface Factory {

    BaseNPC create(NpcContext context) throws GameException;
  }
}
