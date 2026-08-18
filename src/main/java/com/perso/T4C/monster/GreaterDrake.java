package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GreaterDrake extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String CANONICAL_NAME = "Greater Drake";

  public GreaterDrake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Greater Drake",
        "${monster.greater_drake}",
        54943,
        0,
        0,
        0,
        676,
        1533,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        899,
        2750,
        java.util.List.of(),
        false,
        0.0f,
        515,
        464,
        464,
        615,
        0,
        464,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        500,
        2010,
        0,
        1081032704,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d858+675", 6010, 75, 0, 0, 1),
            new MonsterDef.Attack("", 0, 75, 10095, 2, 20),
            new MonsterDef.Attack("", 0, 25, 10616, 0, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
