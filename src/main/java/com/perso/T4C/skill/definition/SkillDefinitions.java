package com.perso.T4C.skill.definition;

import com.perso.T4C.skill.SkillDefinition;
import java.util.List;

public final class SkillDefinitions {
  private SkillDefinitions() {}

  public static List<SkillDefinition> all() {
    return List.of(
        Attack.definition(),
        Dodge.definition(),
        Archery.definition(),
        PowerfulBlow.definition(),
        StunBlow.definition(),
        Parry.definition(),
        ArmorPenetration.definition(),
        TwoWeapons.definition(),
        RapidHealing.definition(),
        PickLock.definition(),
        Peek.definition(),
        Rob.definition(),
        Sneak.definition());
  }
}
