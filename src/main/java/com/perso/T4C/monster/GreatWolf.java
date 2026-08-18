package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GreatWolf extends DataMonster {
  public static final String SOUND_ATTACK = "Wolf Attack.wav";
  public static final String SOUND_DEATH = "Wolf Dying.wav";
  public static final String SOUND_HIT = "Wolf Hit.wav";

  public static final String CANONICAL_NAME = "Great Wolf";

  public GreatWolf(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Great Wolf",
        "${monster.great_wolf}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "Wolf#i",
        "WolfA#i",
        "WolfC#n",
        "Wolf Attack.wav",
        "Wolf Dying.wav",
        "Wolf Hit.wav",
        116,
        357,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Finely cut limestone", 5.0E-4f),
            new MonsterDef.LootDrop("Finely cut emerald", 0.002f),
            new MonsterDef.LootDrop("Finely cut malachite", 0.01f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {63, 63, 63, 63, 42, 5000, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20045,
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
            new MonsterDef.Attack("1d99+77", 790, 87, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10376, 0, 0),
            new MonsterDef.Attack("1d138+107", 970, 7, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
