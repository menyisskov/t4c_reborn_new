package com.perso.T4C.combat;

import com.perso.T4C.item.InventoryService;
import com.perso.T4C.monster.core.BaseMonster;
import com.perso.T4C.npc.core.BaseNPC;
import com.perso.T4C.player.Player;
import java.util.Map;

public final class CombatProfiles {
  private CombatProfiles() {}

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
        player.getLevel(),
        player.getEffectiveStrength(),
        player.getEffectiveEndurance(),
        player.getEffectiveDexterity(),
        Math.max(1, attack),
        Math.max(1, archery),
        dodge,
        ArmorClassRules.effectiveArmorClass(player),
        player.isStunned(),
        player.isHidden(),
        InventoryService.hasMainHandWeapon(player),
        skills);
  }

  public static CombatProfile fromMonster(BaseMonster monster) {
    if (monster == null) throw new IllegalArgumentException("Monster is required");
    return new CombatProfile(
        monster.getCombatLevel(),
        monster.getCombatStrength(),
        monster.getCombatEndurance(),
        monster.getCombatAgility(),
        monster.getCombatAttack(),
        monster.getCombatAttack(),
        monster.getCombatDodge(),
        monster.rollCombatArmorClass(),
        monster.isStunned(),
        false,
        true,
        Map.of());
  }

  public static CombatProfile fromNpc(BaseNPC npc) {
    if (npc == null) throw new IllegalArgumentException("NPC is required");
    int level = Math.max(1, npc.getLevel());
    int attack = Math.max(1, npc.getSkillLevel("attack"));
    int dodge = Math.max(1, npc.getSkillLevel("dodge"));
    return new CombatProfile(
        level,
        Math.max(10, npc.getStrength()),
        Math.max(10, npc.getEndurance()),
        Math.max(10, npc.getDexterity()),
        attack,
        attack,
        dodge,
        npc.getArmorClass(),
        false,
        false,
        true,
        Map.of());
  }

  public static CombatProfile fromCompanion(BaseNPC companion) {
    if (companion == null) throw new IllegalArgumentException("Companion is required");
    int level = Math.max(1, companion.getLevel());
    int agility = 14 + level;
    int attack = agility + level;
    int dodge = Math.max(1, level * 4);
    return new CombatProfile(
        level,
        10 + level,
        10 + level,
        agility,
        attack,
        attack,
        dodge,
        0,
        false,
        false,
        true,
        Map.of());
  }
}
