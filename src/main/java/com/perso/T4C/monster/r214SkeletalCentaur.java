package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Skeletal Centaur", x = 423, y = 503, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 427, y = 552, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 438, y = 508, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 449, y = 556, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 459, y = 483, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 476, y = 580, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 481, y = 604, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 496, y = 447, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 503, y = 599, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 504, y = 426, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 512, y = 453, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 526, y = 578, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 550, y = 558, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 552, y = 497, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 563, y = 490, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 569, y = 537, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeletal Centaur", x = 577, y = 549, z = 1, stationary = false, aggressive = true)
public final class r214SkeletalCentaur extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r214SkeletalCentaur(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeletal Centaur",
        "${monster.skeletal_centaur}",
        1879,
        0,
        7,
        6776,
        85,
        194,
        30000L,
        "64kCentaurSkeleton#i",
        "64kCentaurSkeletonA#i",
        "64kCentaurSkeletonC!n",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        125,
        385,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough malachite", 0.02f),
            new MonsterDef.LootDrop("Rough limestone", 0.001f),
            new MonsterDef.LootDrop("Rough emerald", 0.004f)),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        0,
        77,
        0,
        new int[] {61, 61, 61, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        70,
        290,
        0,
        1078034432,
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
        36,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d110+84", 850, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 50, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10359, 1, 10),
            new MonsterDef.Attack("", 0, 10, 10357, 1, 10),
            new MonsterDef.Attack("", 0, 20, 10096, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
