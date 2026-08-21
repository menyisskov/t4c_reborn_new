package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Giant Scorpion", x = 118, y = 2534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 125, y = 2503, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 135, y = 2497, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 144, y = 2450, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 149, y = 2519, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 158, y = 2725, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 159, y = 2445, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 167, y = 2494, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 171, y = 2584, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 183, y = 2590, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 201, y = 2584, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 204, y = 1787, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 204, y = 1795, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 212, y = 2394, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 213, y = 2402, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 217, y = 2638, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 237, y = 2572, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 242, y = 2633, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 244, y = 2637, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 258, y = 2781, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 270, y = 2572, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 274, y = 2783, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 300, y = 2464, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 302, y = 2383, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 302, y = 2797, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 307, y = 2784, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 315, y = 2412, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 315, y = 2791, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 318, y = 2454, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 330, y = 2507, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 357, y = 2365, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 369, y = 2613, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 370, y = 2369, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 370, y = 2786, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 375, y = 2787, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 377, y = 2636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 380, y = 2485, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 382, y = 2624, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 414, y = 2647, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 419, y = 2780, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 421, y = 2782, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 430, y = 2508, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 431, y = 2522, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 439, y = 2772, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 461, y = 2394, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 471, y = 2510, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 495, y = 2426, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 495, y = 2460, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 511, y = 2666, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 514, y = 2399, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 517, y = 2655, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 519, y = 2378, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 522, y = 2414, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 524, y = 2451, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 540, y = 1611, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 542, y = 1601, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 549, y = 2554, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 554, y = 2424, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 555, y = 2534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 560, y = 2419, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 671, y = 2379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 686, y = 2380, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 772, y = 1585, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 781, y = 1632, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 809, y = 1520, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Giant Scorpion", x = 870, y = 1533, z = 0, stationary = false, aggressive = true)
public final class GiantScorpion extends DataMonster {
  public static final String SOUND_ATTACK = "Elemear Attack.wav";
  public static final String SOUND_DEATH = "Scorpion Dying.wav";
  public static final String SOUND_HIT = "Scorpion Hit.wav";

  public static final String CANONICAL_NAME = "Giant Scorpion";

  public GiantScorpion(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Giant Scorpion",
        "${monster.giant_scorpion}",
        510,
        0,
        3,
        933,
        24,
        55,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        48,
        148,
        java.util.List.of(),
        false,
        0.0f,
        42,
        39,
        39,
        47,
        0,
        39,
        0,
        new int[] {81, 81, 54, 108, 81, 5000, 100, 100, 100, 100, 100, 100},
        27,
        83,
        0,
        1077149696,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d32+23", 334, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
