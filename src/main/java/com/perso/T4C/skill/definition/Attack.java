package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class Attack {
  private Attack() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("attack", 1, 0, 0, 0, 0, 0, 1, Map.of(), 0L);
  }
}
