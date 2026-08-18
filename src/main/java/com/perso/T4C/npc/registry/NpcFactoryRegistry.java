package com.perso.T4C.npc.registry;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.npc.arakas.LighthavenSamaritan;
import com.perso.T4C.npc.core.*;
import com.perso.T4C.npc.generated.GeneratedNpcIndex;
import com.perso.T4C.npc.script.*;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Supplier;

public final class NpcFactoryRegistry {

  private static final Map<String, Registration> BY_ID = new LinkedHashMap<>();

  static {
    GeneratedNpcIndex.registrations().forEach(NpcFactoryRegistry::register);

    register(
        new Registration(
            LighthavenSamaritan.ID,
            LighthavenSamaritan.DISPLAY_NAME,
            LighthavenSamaritan.SPRITE_BASE,
            LighthavenSamaritan::new));
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

    if (registration.specification() == null && !LighthavenSamaritan.ID.equals(registration.id())) {

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
