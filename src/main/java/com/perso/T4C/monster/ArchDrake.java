package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ArchDrake extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String CANONICAL_NAME = "Arch Drake";

  public ArchDrake(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Arch Drake",
        "${monster.arch_drake}",
        206623,
        0,
        0,
        0,
        1353,
        3067,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Demon Attack.wav",
        "Demon Dying.wav",
        "Demon Hit.wav",
        1799,
        5500,
        java.util.List.of(),
        false,
        0.0f,
        1015,
        914,
        914,
        1215,
        0,
        914,
        0,
        new int[] {63, 5000, 63, -63, 63, 5000, 100, 100, 100, 100, 100, 100},
        1000,
        4010,
        0,
        1082081280,
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
            new MonsterDef.Attack("1d1715+1352", 12010, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10095, 2, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
