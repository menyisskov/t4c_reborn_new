package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BrokenOne extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Broken One";

  public BrokenOne(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Broken One",
        "${monster.broken_one}",
        876,
        0,
        5,
        2203,
        43,
        98,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        73,
        225,
        java.util.List.of(new MonsterDef.LootDrop("Gleaming shard", 0.02f)),
        false,
        0.0f,
        56,
        51,
        51,
        64,
        0,
        51,
        0,
        new int[] {74, 74, 99, 49, 5025, 49, 100, 100, 100, 100, 100, 100},
        41,
        174,
        0,
        1077149696,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d56+42", 502, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 30, 10119, 7, 15),
            new MonsterDef.Attack("", 0, 45, 10086, 7, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
