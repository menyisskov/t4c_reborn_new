package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.Map;

public final class ArmorPenetration {
  private ArmorPenetration() {}

  public static SkillDefinition definition() {
    return new SkillDefinition("armor_penetration", 25, 75, 0, 40, 30, 0, 1, Map.of(), 0L);
  }
}
