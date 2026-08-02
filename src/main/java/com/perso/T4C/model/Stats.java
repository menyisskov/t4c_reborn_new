package com.perso.T4C.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import lombok.AccessLevel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shared character stats for player and NPC.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Stats {
    // Primary attributes
    protected int strength;
    protected int dexterity;
    protected int endurance;
    protected int intelligence;
    protected int wisdom;
    protected int gold;
    /** Alignment value used by USER_KARMA in original NPC decision trees. */
    protected int karma;

    // Resources
    protected int maxHp;
    protected int currentHp;
    protected int maxMana;
    protected int mana;

    // Progression
    protected int level;
    protected int currentXp;
    protected int xpToNextLevel;
    protected int statPoints;
    protected int skillPoints;

    protected List<String> spells;
    protected List<QuickSlotEntry> quickSlots;

    /** Discrete skill levels (T4C skills: attack, dodge, archery, stun_blow, etc.). */
    protected Map<String, Integer> skills = new HashMap<>();

    public int getSkillLevel(String skillId) {
        if (skillId == null || skills == null) return 0;
        return skills.getOrDefault(skillId, 0);
    }

    public void setSkillLevel(String skillId, int level) {
        if (skillId == null) return;
        if (skills == null) skills = new HashMap<>();
        skills.put(skillId, Math.max(0, level));
    }
}
