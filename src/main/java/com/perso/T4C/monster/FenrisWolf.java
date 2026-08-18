package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FenrisWolf extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Fenris Wolf";

  public FenrisWolf(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Fenris Wolf",
        "${monster.fenris_wolf}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC#n",
        "Wolf Attack.wav",
        "Wolf Dying.wav",
        "Wolf Hit.wav",
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Finely cut agate", 0.01f),
            new MonsterDef.LootDrop("Finely cut diamond", 0.002f),
            new MonsterDef.LootDrop("Finely cut moonstone", 5.0E-4f),
            new MonsterDef.LootDrop("Prismatic armband", 0.02f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {56, 56, 56, 56, 37, 5000, 100, 100, 100, 100, 100, 100},
        80,
        330,
        0,
        1078198272,
        20046,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        33,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10119, 2, 12),
            new MonsterDef.Attack("", 0, 24, 10094, 2, 12),
            new MonsterDef.Attack("", 0, 40, 10374, 2, 12),
            new MonsterDef.Attack("", 0, 3, 10352, 2, 12),
            new MonsterDef.Attack("", 0, 3, 10317, 2, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
