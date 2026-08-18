package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class Parry {
  private Parry() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("parry", 10, 0, 0, 30, 20, 0, 1, Map.of(), 0L);
  }
}
