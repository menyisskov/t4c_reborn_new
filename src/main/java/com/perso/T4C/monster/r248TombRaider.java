package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Tomb Raider", x = 1121, y = 129, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Tomb Raider", x = 21, y = 461, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Tomb Raider", x = 428, y = 204, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Tomb Raider", x = 71, y = 620, z = 1, stationary = false, aggressive = true)
public final class r248TombRaider extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r248TombRaider(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Tomb Raider",
        "${monster.tomb_raider}",
        264,
        0,
        2,
        272,
        7,
        17,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        28,
        88,
        java.util.List.of(
            new MonsterDef.LootDrop("Torch", 0.1f),
            new MonsterDef.LootDrop("Leather pants", 0.03f),
            new MonsterDef.LootDrop("Dagger of Piercing", 0.05f),
            new MonsterDef.LootDrop("Iron key", 0.005f),
            new MonsterDef.LootDrop("Healing potion", 0.1f),
            new MonsterDef.LootDrop("Shimmering white robe", 0.01f)),
        false,
        0.0f,
        22,
        22,
        22,
        22,
        0,
        22,
        0,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        8,
        47,
        0,
        1074266112,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        10,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d11+6", 106, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
