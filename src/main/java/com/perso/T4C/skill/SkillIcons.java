package com.perso.T4C.skill;

import java.util.Map;

public final class SkillIcons {
  private static final Map<String, String> BY_SKILL_ID =
      Map.ofEntries(
          Map.entry("attack", "64kIconSword"),
          Map.entry("archery", "64kIconBow"),
          Map.entry("dodge", "64kIconShield"),
          Map.entry("parry", "64kIconParry"),
          Map.entry("stun_blow", "64kIconStunBlow"),
          Map.entry("powerful_blow", "64kIconPowerBlow"),
          Map.entry("first_aid", "64kIconFirstAid"),
          Map.entry("rapid_healing", "64kIconRapidHealing"),
          Map.entry("hide", "64kIconHide"),
          Map.entry("meditate", "64kIconMeditate"),
          Map.entry("sneak", "64kIconSneak"),
          Map.entry("search", "64kIconSearch"),
          Map.entry("peek", "64kIconPeek"),
          Map.entry("picklock", "64kIconPicklock"),
          Map.entry("armor_penetration", "64kIconArmorPierce"),
          Map.entry("rob", "64kIconRob"));

  private SkillIcons() {}

  public static String spriteFor(String skillId) {
    return skillId == null ? null : BY_SKILL_ID.get(skillId);
  }
}
