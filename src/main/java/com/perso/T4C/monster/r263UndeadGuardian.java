package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r263UndeadGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r263UndeadGuardian(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Undead Guardian",
        "${monster.undead_guardian}",
        6334,
        0,
        11,
        34955,
        203,
        460,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC#n",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        269,
        825,
        java.util.List.of(),
        false,
        0.0f,
        165,
        149,
        149,
        195,
        0,
        149,
        0,
        new int[] {47, 47, 63, 31, 5025, 47, 100, 100, 100, 100, 100, 100},
        150,
        610,
        0,
        1079164928,
        20063,
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
        java.util.List.of(
            new MonsterDef.Attack("1d258+202", 1810, 50, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10090, 0, 25)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
