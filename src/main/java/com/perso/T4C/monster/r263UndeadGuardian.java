package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Undead Guardian", x = 2165, y = 295, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2265, y = 195, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2265, y = 395, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2365, y = 295, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2585, y = 613, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2586, y = 686, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2668, y = 766, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2678, y = 519, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2732, y = 832, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2800, y = 503, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2807, y = 791, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2862, y = 561, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2869, y = 729, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2920, y = 681, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Undead Guardian", x = 2922, y = 621, z = 1, stationary = false, aggressive = true)
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
        "64kCentaurSkeletonC!n",
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
