package com.perso.T4C.combat;

import java.util.Collections;
import java.util.Map;

/** Immutable combat values used by the rules engine. */
public record CombatProfile(
        int level,
        int strength,
        int endurance,
        int agility,
        int attack,
        int archery,
        int dodge,
        double armorClass,
        boolean stunned,
        boolean hidden,
        boolean weaponEquipped,
        Map<String, Integer> skills) {

    public CombatProfile {
        level = Math.max(1, level);
        strength = Math.max(1, strength);
        endurance = Math.max(1, endurance);
        agility = Math.max(1, agility);
        attack = Math.max(1, attack);
        archery = Math.max(1, archery);
        dodge = Math.max(1, dodge);
        armorClass = Math.max(0d, armorClass);
        skills = skills == null ? Collections.emptyMap() : Map.copyOf(skills);
    }

    public int skill(String id) {
        return id == null ? 0 : Math.max(0, skills.getOrDefault(id, 0));
    }
}
