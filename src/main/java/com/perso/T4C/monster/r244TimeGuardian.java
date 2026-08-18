package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r244TimeGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r244TimeGuardian(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Time Guardian",
        "${monster.time_guardian}",
        5641,
        0,
        10,
        30115,
        149,
        389,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        251,
        770,
        java.util.List.of(),
        false,
        0.0f,
        155,
        140,
        140,
        183,
        0,
        140,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        140,
        570,
        0,
        1079083008,
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
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d241+148", 1690, 100, 0, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
