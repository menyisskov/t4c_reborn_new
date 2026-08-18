package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class PowerfulBlow {
  private PowerfulBlow() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("powerful_blow", 15, 50, 0, 30, 0, 0, 1, Map.of(), 0L);
  }
}
