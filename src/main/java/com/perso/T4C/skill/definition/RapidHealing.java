package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class RapidHealing {
  private RapidHealing() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("rapid_healing", 30, 0, 80, 0, 0, 0, 1, Map.of(), 5000L);
  }
}
