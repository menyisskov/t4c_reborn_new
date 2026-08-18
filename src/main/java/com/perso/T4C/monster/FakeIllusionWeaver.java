package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FakeIllusionWeaver extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "FakeIllusionWeaver";

  public FakeIllusionWeaver(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "FakeIllusionWeaver",
        "${monster.fakeillusionweaver}",
        10,
        0,
        0,
        1,
        0,
        0,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        49,
        45,
        45,
        55,
        0,
        45,
        0,
        new int[] {78, 78, 78, 78, 103, 5000, 100, 100, 100, 100, 100, 100},
        34,
        146,
        0,
        0,
        10002,
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
            new MonsterDef.Attack("", 0, 100, 0, 0, 0),
            new MonsterDef.Attack("1d42+32", 418, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10094, 5, 12),
            new MonsterDef.Attack("", 0, 30, 10119, 5, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
