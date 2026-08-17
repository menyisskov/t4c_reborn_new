package com.perso.T4C.skill;

import java.util.Map;

/**
 * Skill id → icon sprite name, the Java port of the original client's {@code SkillIcons}
 * table (see GoN {@code VisualObjectList.cpp}, the {@code SkillIcons.BindSprite} calls).
 *
 * <p>Only the skills bound to a {@code 64kIcon*} sprite present in the sprite pack appear
 * here; {@link #spriteFor(String)} returns {@code null} for the others, letting callers
 * render the row without an icon rather than forcing a placeholder.
 */
public final class SkillIcons {

    private static final Map<String, String> BY_SKILL_ID = Map.ofEntries(
            Map.entry("attack",            "64kIconSword"),
            Map.entry("archery",           "64kIconBow"),
            Map.entry("dodge",             "64kIconShield"),
            Map.entry("parry",             "64kIconParry"),
            Map.entry("stun_blow",         "64kIconStunBlow"),
            Map.entry("powerful_blow",     "64kIconPowerBlow"),
            Map.entry("first_aid",         "64kIconFirstAid"),
            Map.entry("rapid_healing",     "64kIconRapidHealing"),
            Map.entry("hide",              "64kIconHide"),
            Map.entry("meditate",          "64kIconMeditate"),
            Map.entry("sneak",             "64kIconSneak"),
            Map.entry("search",            "64kIconSearch"),
            Map.entry("peek",              "64kIconPeek"),
            Map.entry("picklock",          "64kIconPicklock"),
            Map.entry("armor_penetration", "64kIconArmorPierce"),
            Map.entry("rob",               "64kIconRob"));

    private SkillIcons() {
    }

    /** Icon sprite name for a skill id, or {@code null} when the skill has no bound icon. */
    public static String spriteFor(String skillId) {
        return skillId == null ? null : BY_SKILL_ID.get(skillId);
    }
}
