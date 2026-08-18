package com.perso.T4C.skill;

import com.perso.T4C.config.Paths;
import com.perso.T4C.helper.SkillDefBinaryIO;
import java.io.File;
import java.io.IOException;
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
    File file = new File(Paths.SKILLS_BIN);
    if (!file.exists()) {
      rebuild(List.of());
      return cache;
    }
    try {
      rebuild(SkillDefBinaryIO.read(file));
    } catch (Exception ignored) {
      rebuild(List.of());
    }
    return cache;
  }

  public static synchronized void save(List<SkillDefinition> defs) throws IOException {
    SkillDefBinaryIO.write(new File(Paths.SKILLS_BIN), defs);
    rebuild(defs);
  }

  public static synchronized SkillDefinition findById(String id) {
    load();
    return id == null ? null : cache.get(id);
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
