package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Skeleton", x = 100, y = 620, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 103, y = 634, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 113, y = 682, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2767, y = 1087, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2772, y = 1086, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2774, y = 1109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2775, y = 1076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2781, y = 1115, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2782, y = 1071, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2782, y = 1091, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2782, y = 1114, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2785, y = 1106, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2790, y = 1073, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2790, y = 1082, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2798, y = 1100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2802, y = 1095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 2812, y = 290, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 34, y = 447, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 464, y = 154, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 469, y = 140, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 478, y = 161, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 50, y = 461, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 57, y = 488, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 59, y = 627, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 63, y = 523, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 69, y = 639, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 84, y = 606, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 86, y = 635, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Skeleton", x = 89, y = 609, z = 1, stationary = false, aggressive = true)
public final class r215Skeleton extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Skeleton Dying.wav";
  public static final String SOUND_HIT = "Skeleton Hit.wav";

  public r215Skeleton(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Skeleton",
        "${monster.skeleton}",
        69,
        0,
        1,
        59,
        4,
        10,
        30000L,
        "Skeleton#g",
        "SkeletonA#i",
        "SkeletonC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        7,
        22,
        java.util.List.of(
            new MonsterDef.LootDrop("Leather gloves", 0.03f),
            new MonsterDef.LootDrop("Ring of confidence", 0.01f),
            new MonsterDef.LootDrop("Healing potion", 0.02f),
            new MonsterDef.LootDrop("Light healing potion", 0.03f),
            new MonsterDef.LootDrop("Skeleton bone", 7.0E-4f)),
        false,
        0.0f,
        18,
        18,
        18,
        18,
        0,
        18,
        0,
        new int[] {92, 92, 122, 61, 5025, 61, 100, 100, 100, 100, 100, 100},
        4,
        26,
        0,
        1073741824,
        20012,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        14,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d7+3", 58, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
