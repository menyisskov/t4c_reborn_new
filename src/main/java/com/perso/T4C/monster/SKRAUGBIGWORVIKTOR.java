package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1011, y = 580, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1012, y = 581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1183, y = 419, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1184, y = 420, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1243, y = 501, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 1244, y = 502, z = 0, stationary = false, aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2072,
    y = 1406,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2073,
    y = 1407,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2192,
    y = 1568,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2193,
    y = 1569,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2303,
    y = 1500,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2304,
    y = 1501,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2511, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2511, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2511, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2512, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2513, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 633, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2514, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 633, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2515, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 633, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2516, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2517, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2518, y = 640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 641, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 636, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2520, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 637, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2520, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2519, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2520, y = 639, z = 2, stationary = false, aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2579,
    y = 1336,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGBIGWORVIKTOR",
    x = 2580,
    y = 1337,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2859, y = 382, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 2860, y = 383, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 798, y = 512, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 799, y = 513, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 819, y = 445, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 820, y = 446, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 844, y = 561, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 845, y = 562, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 870, y = 436, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 871, y = 437, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 879, y = 551, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 880, y = 552, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 930, y = 412, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 931, y = 413, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 954, y = 577, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 955, y = 578, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 984, y = 450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBIGWORVIKTOR", x = 985, y = 451, z = 0, stationary = false, aggressive = true)
public final class SKRAUGBIGWORVIKTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGBIGWORVIKTOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGBIGWORVIKTOR",
        "${monster.skraugbigworviktor}",
        2301,
        0,
        40,
        47045,
        108,
        245,
        30000L,
        "64kSkavenWarrior#i",
        "64kSkavenWarriorA#k",
        "64kSkavenWarriorC!s",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(150),
        MonsterGoldCurve.goldMax(150),
        java.util.List.of(
            new MonsterDef.LootDrop("moon_tug_scalp", 0.3f),
            new MonsterDef.LootDrop("raw_crystal", 0.3f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        86,
        86,
        31,
        new int[] {56, 56, 56, 56, 56, 5000, 100, 100, 100, 100, 100, 100},
        150,
        330,
        0,
        1109393408,
        20050,
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
        java.util.List.of(new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
