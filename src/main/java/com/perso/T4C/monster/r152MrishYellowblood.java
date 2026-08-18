package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Mrish Yellowblood", x = 864, y = 2421, z = 2, stationary = false, aggressive = true)
public final class r152MrishYellowblood extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public r152MrishYellowblood(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mrish Yellowblood",
        "${monster.mrish_yellowblood}",
        626,
        0,
        2,
        906,
        15,
        35,
        30000L,
        "GoblinBoss#l",
        "GoblinBossA#i",
        "GoblinBossC#l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        64,
        198,
        java.util.List.of(
            new MonsterDef.LootDrop("Studded leather armor", 0.02f),
            new MonsterDef.LootDrop("Studded leather pants", 0.03f),
            new MonsterDef.LootDrop("Studded leather boots", 0.03f),
            new MonsterDef.LootDrop("Ringmail gauntlets", 0.04f),
            new MonsterDef.LootDrop("Ringmail boots", 0.04f),
            new MonsterDef.LootDrop("Goblin Blade", 0.03f),
            new MonsterDef.LootDrop("Ring of the bear", 0.04f),
            new MonsterDef.LootDrop("Round shield", 0.05f),
            new MonsterDef.LootDrop("Healing potion", 0.03f),
            new MonsterDef.LootDrop("Red cape", 0.04f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {113, 56, 85, 85, 85, 5025, 100, 100, 100, 100, 100, 100},
        18,
        82,
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
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
