package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Sand Worm", x = 125, y = 2524, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 140, y = 2511, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 151, y = 2547, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 156, y = 2476, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 161, y = 2531, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 168, y = 2508, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 220, y = 2443, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 221, y = 2607, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 222, y = 2654, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 241, y = 2663, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 244, y = 2502, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 250, y = 2501, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 250, y = 2550, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 255, y = 2385, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 258, y = 2428, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 260, y = 2418, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 268, y = 2547, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 276, y = 2609, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 279, y = 2488, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 286, y = 2589, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 287, y = 2620, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 293, y = 2431, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 293, y = 2658, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 320, y = 2586, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 325, y = 2400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 325, y = 2652, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 330, y = 2531, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 332, y = 2667, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 335, y = 2482, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 351, y = 2434, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 360, y = 2466, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 377, y = 2535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 381, y = 2661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 404, y = 2502, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 408, y = 2484, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 415, y = 2554, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 438, y = 2420, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 445, y = 2473, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 446, y = 2538, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 471, y = 2444, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 493, y = 2480, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Sand Worm", x = 506, y = 2457, z = 0, stationary = false, aggressive = true)
public final class r207SandWorm extends DataMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public r207SandWorm(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Sand Worm",
        "${monster.sand_worm}",
        533,
        0,
        3,
        1004,
        25,
        58,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        50,
        154,
        java.util.List.of(),
        false,
        0.0f,
        43,
        40,
        40,
        48,
        0,
        40,
        0,
        new int[] {80, 80, 53, 107, 80, 5000, 100, 100, 100, 100, 100, 100},
        28,
        92,
        0,
        1077149696,
        20016,
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
        java.util.List.of(new MonsterDef.Attack("1d34+24", 346, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
