package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class Rob {
  private Rob() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("rob", 17, 0, 0, 50, 0, 0, 1, Map.of("peek", 25), 1000L);
  }
}
