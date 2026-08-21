package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class Cobra extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public static final String CANONICAL_NAME = "Cobra";

  public Cobra(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Cobra",
        "${monster.cobra}",
        657,
        0,
        4,
        1390,
        31,
        71,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        59,
        181,
        java.util.List.of(),
        false,
        0.0f,
        48,
        44,
        44,
        54,
        0,
        44,
        0,
        new int[] {78, 78, 52, 104, 78, 5000, 100, 100, 100, 100, 100, 100},
        33,
        142,
        0,
        1075314688,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        15,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d41+30", 456, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
