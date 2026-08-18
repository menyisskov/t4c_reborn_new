package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Trish Yellowblood", x = 863, y = 2615, z = 2, stationary = false, aggressive = true)
public final class r249TrishYellowblood extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r249TrishYellowblood(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Trish Yellowblood",
        "${monster.trish_yellowblood}",
        668,
        0,
        2,
        993,
        16,
        37,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        68,
        208,
        java.util.List.of(
            new MonsterDef.LootDrop("Studded leather armor", 0.02f),
            new MonsterDef.LootDrop("Studded leather pants", 0.03f),
            new MonsterDef.LootDrop("Studded leather boots", 0.03f),
            new MonsterDef.LootDrop("Ringmail gauntlets", 0.04f),
            new MonsterDef.LootDrop("Ringmail boots", 0.04f),
            new MonsterDef.LootDrop("Goblin Blade", 0.03f),
            new MonsterDef.LootDrop("Ring of the bear", 0.04f)),
        false,
        0.0f,
        34,
        32,
        32,
        37,
        0,
        32,
        0,
        new int[] {113, 56, 85, 85, 85, 5025, 100, 100, 100, 100, 100, 100},
        19,
        86,
        0,
        1075970048,
        20041,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d22+15", 238, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
