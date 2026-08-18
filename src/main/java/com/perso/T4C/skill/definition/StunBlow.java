package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class StunBlow {
  private StunBlow() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("stun_blow", 3, 25, 0, 20, 0, 0, 1, Map.of(), 0L);
  }
}
