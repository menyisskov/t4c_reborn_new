package com.perso.T4C.combat;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.BaseMonster;
import com.perso.T4C.npc.BaseNPC;
import com.perso.T4C.player.Player;

import java.util.Map;

/** Adapters between runtime entities and the pure combat engine. */
public final class CombatProfiles {
    private CombatProfiles() {
    }

    public static CombatProfile fromPlayer(Player player) {
        if (player == null) throw new IllegalArgumentException("Player is required");
        Map<String, Integer> skills = player.getSkills() == null ? Map.of() : player.getSkills();
        int attack = player.getEffectiveSkillLevel("attack");
        int archery = player.getEffectiveSkillLevel("archery");
        int dodgeSkill = player.getEffectiveSkillLevel("dodge");
        if (attack <= 0) attack = 1;
        if (archery <= 0) archery = 1;
        if (dodgeSkill <= 0) dodgeSkill = 1;
        int dodge = Math.max(1, dodgeSkill - InventoryService.equippedDodgePenalty(player));
        return new CombatProfile(
                player.getLevel(), player.getEffectiveStrength(), player.getEffectiveEndurance(),
                player.getEffectiveDexterity(), Math.max(1, attack),
                Math.max(1, archery), dodge,
                ArmorClassRules.effectiveArmorClass(player),
                player.isStunned(), player.isHidden(), InventoryService.hasWeapon(player), skills);
    }

    public static CombatProfile fromMonster(BaseMonster monster) {
        if (monster == null) throw new IllegalArgumentException("Monster is required");
        return new CombatProfile(monster.getCombatLevel(), monster.getCombatStrength(),
                monster.getCombatEndurance(), monster.getCombatAgility(), monster.getCombatAttack(),
                monster.getCombatAttack(), monster.getCombatDodge(), monster.rollCombatArmorClass(),
                monster.isStunned(), false, true, Map.of());
    }

    /**
     * NPCs were never given a monster-style combat kit (no per-instance
     * str/end/agi/attack/dodge data), so hostile NPCs use a flat, modest
     * profile approximating a low-level guard rather than reading unset stats.
     */
    public static CombatProfile fromNpc(BaseNPC npc) {
        if (npc == null) throw new IllegalArgumentException("NPC is required");
        int level = Math.max(1, npc.getLevel());
        return new CombatProfile(level, 10, 10, 10, 10, 10, 10, 0, false, false, true, Map.of());
    }
}
