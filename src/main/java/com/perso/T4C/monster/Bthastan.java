package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Bthastan extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Bthastan";

  public Bthastan(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bthastan",
        "${monster.bthastan}",
        3259,
        0,
        9,
        14831,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
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
        0,
        104,
        0,
        new int[] {47, 62, 47, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        100,
        410,
        0,
        1078525952,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 50, 10095, 10, 20),
            new MonsterDef.Attack("", 0, 50, 10096, 2, 9),
            new MonsterDef.Attack("", 0, 40, 10088, 2, 20),
            new MonsterDef.Attack("", 0, 10, 10382, 2, 20),
            new MonsterDef.Attack("1d172+109", 1210, 100, 0, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
