package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class KAHPLETHGUARD11 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "KAHPLETHGUARD11";

  public KAHPLETHGUARD11(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "KAHPLETHGUARD11",
        "${monster.kahplethguard11}",
        32590,
        0,
        4,
        74158,
        135,
        306,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC#s",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        179,
        550,
        java.util.List.of(),
        false,
        0.0f,
        115,
        104,
        104,
        135,
        104,
        104,
        35,
        new int[] {47, 47, 47, 47, 47, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1112014848,
        20050,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d172+134", 1210, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
