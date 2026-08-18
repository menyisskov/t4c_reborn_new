package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class Sneak {
  private Sneak() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("sneak", 24, 0, 0, 75, 0, 0, 1, Map.of(), 1000L);
  }
}
