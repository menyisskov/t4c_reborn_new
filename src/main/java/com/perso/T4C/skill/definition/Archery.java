package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class Archery {
  private Archery() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("archery", 1, 0, 0, 0, 0, 0, 1, Map.of(), 0L);
  }
}
