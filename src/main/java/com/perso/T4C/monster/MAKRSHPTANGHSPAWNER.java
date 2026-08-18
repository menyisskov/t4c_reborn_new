package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "MAKRSHPTANGHSPAWNER",
    x = 2215,
    y = 245,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MAKRSHPTANGHSPAWNER",
    x = 2215,
    y = 345,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MAKRSHPTANGHSPAWNER",
    x = 2315,
    y = 245,
    z = 1,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "MAKRSHPTANGHSPAWNER",
    x = 2315,
    y = 345,
    z = 1,
    stationary = false,
    aggressive = true)
public final class MAKRSHPTANGHSPAWNER extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public MAKRSHPTANGHSPAWNER(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MAKRSHPTANGHSPAWNER",
        "${monster.makrshptanghspawner}",
        6334,
        0,
        15,
        45000,
        203,
        460,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC#n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        269,
        825,
        java.util.List.of(),
        false,
        0.0f,
        165,
        149,
        149,
        195,
        149,
        149,
        45,
        new int[] {47, 47, 63, 31, 5025, 47, 100, 100, 100, 100, 100, 100},
        150,
        610,
        0,
        1117126656,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d258+202", 1810, 50, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
