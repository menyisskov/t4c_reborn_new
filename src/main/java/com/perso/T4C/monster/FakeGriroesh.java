package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class FakeGriroesh extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "FakeGriroesh";

  public FakeGriroesh(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "FakeGriroesh",
        "${monster.fakegriroesh}",
        10,
        0,
        0,
        1,
        34,
        78,
        30000L,
        "Demon#i",
        "DemonA#i",
        "DemonC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {103, 103, 103, 103, 103, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        0,
        20013,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10095, 3, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
