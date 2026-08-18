package com.perso.T4C.skill;

import com.perso.T4C.skill.definition.SkillDefinitions;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class SkillRegistry {
  private static Map<String, SkillDefinition> cache;

  private SkillRegistry() {}

  public static synchronized Map<String, SkillDefinition> load() {
    if (cache != null) {
      return cache;
    }
    rebuild(SkillDefinitions.all());
    return cache;
  }

  public static synchronized SkillDefinition findById(String id) {
    load();
    return id == null ? null : cache.get(id);
  }

  public static synchronized void save(List<SkillDefinition> defs) {
    rebuild(defs);
  }

  private static void rebuild(List<SkillDefinition> defs) {
    Map<String, SkillDefinition> map = new LinkedHashMap<>();
    for (SkillDefinition def : defs) {
      if (def != null && def.id() != null && !def.id().isBlank()) {
        map.put(def.id(), def);
      }
    }
    cache = Map.copyOf(map);
  }
}
