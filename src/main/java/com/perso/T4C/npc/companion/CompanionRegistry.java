package com.perso.T4C.npc.companion;

import com.perso.T4C.npc.companion.definition.CompanionDefinitions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CompanionRegistry {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  private static List<CompanionDef> cache;

  private static Map<String, CompanionDef> byId;

  private CompanionRegistry() {}

  public static synchronized List<CompanionDef> load() {

    if (cache != null) {

      return cache;
    }

    rebuild(CompanionDefinitions.all());

    return cache;
  }

  public static synchronized void save(List<CompanionDef> defs) {
    rebuild(defs);
  }

  public static synchronized CompanionDef findById(String id) {

    load();

    return id == null ? null : byId.get(id);
  }

  public static synchronized void invalidate() {

    cache = null;

    byId = null;
  }

  private static void rebuild(List<CompanionDef> defs) {

    cache = List.copyOf(defs);

    Map<String, CompanionDef> map = new LinkedHashMap<>();

    for (CompanionDef def : cache) {

      if (def != null && def.getId() != null) {

        map.put(def.getId(), def);
      }
    }

    byId = map;
  }
}
