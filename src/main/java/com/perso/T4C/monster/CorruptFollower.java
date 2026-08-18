package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CorruptFollower extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Corrupt Follower";

  public CorruptFollower(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Corrupt Follower",
        "${monster.corrupt_follower}",
        463,
        0,
        3,
        810,
        22,
        50,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        44,
        137,
        java.util.List.of(),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        0,
        37,
        0,
        new int[] {54, 109, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1076363264,
        20034,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
