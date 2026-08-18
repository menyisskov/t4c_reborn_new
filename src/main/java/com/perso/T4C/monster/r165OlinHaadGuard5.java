package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Olin Haad Guard 5", x = 2923, y = 1351, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2931, y = 1359, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2937, y = 1334, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2945, y = 1365, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2946, y = 1370, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2949, y = 1329, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2949, y = 1367, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2949, y = 1374, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2953, y = 1372, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2954, y = 1377, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2959, y = 1387, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2961, y = 1312, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2968, y = 1396, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2978, y = 1388, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 298, y = 476, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2982, y = 1312, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2985, y = 1379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 2998, y = 1326, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 3000, y = 1366, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 3009, y = 1339, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 3011, y = 1352, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 319, y = 484, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 320, y = 469, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 334, y = 479, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 345, y = 468, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 349, y = 478, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 351, y = 473, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Olin Haad Guard 5", x = 361, y = 486, z = 2, stationary = false, aggressive = true)
public final class r165OlinHaadGuard5 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r165OlinHaadGuard5(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Guard 5",
        "${monster.olin_haad_guard_5}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        8,
        27,
        java.util.List.of(),
        false,
        0.0f,
        20,
        19,
        19,
        21,
        0,
        19,
        0,
        new int[] {91, 91, 91, 91, 60, 5000, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
        20006,
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
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
