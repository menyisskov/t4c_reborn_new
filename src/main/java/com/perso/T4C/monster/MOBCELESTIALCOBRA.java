package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBCELESTIALCOBRA", x = 1804, y = 2870, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1820, y = 2760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1839, y = 2863, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1843, y = 2760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1847, y = 2797, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1854, y = 2877, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1860, y = 2705, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1865, y = 2697, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1866, y = 2755, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1866, y = 2858, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1869, y = 2914, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1873, y = 2918, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1875, y = 2768, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1877, y = 2847, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1881, y = 2790, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1881, y = 2867, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1886, y = 2962, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1889, y = 2938, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1914, y = 2752, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1920, y = 2928, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1922, y = 2754, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1932, y = 2950, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1933, y = 2924, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1942, y = 2716, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1961, y = 2914, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1965, y = 2910, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1980, y = 2709, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1983, y = 2894, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 1988, y = 2694, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2004, y = 2765, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2005, y = 2769, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2012, y = 2898, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2049, y = 2811, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2065, y = 2809, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2066, y = 2775, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBCELESTIALCOBRA", x = 2072, y = 2792, z = 0, stationary = false, aggressive = true)
public final class MOBCELESTIALCOBRA extends DataMonster {
  public static final String SOUND_ATTACK = "Snake Attack.wav";
  public static final String SOUND_DEATH = "Snake Dying.wav";
  public static final String SOUND_HIT = "Snake Hit.wav";

  public MOBCELESTIALCOBRA(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "MOBCELESTIALCOBRA",
        "${monster.mobcelestialcobra}",
        2761,
        0,
        43,
        59738,
        121,
        276,
        30000L,
        "Snake#h",
        "SnakeA#g",
        "SnakeC#m",
        "Snake Attack.wav",
        "Snake Dying.wav",
        "Snake Hit.wav",
        161,
        495,
        java.util.List.of(),
        false,
        0.0f,
        105,
        95,
        95,
        123,
        95,
        95,
        33,
        new int[] {61, 61, 41, 82, 61, 5000, 100, 100, 100, 100, 100, 100},
        90,
        370,
        0,
        1110704128,
        20019,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d156+120", 1090, 10, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
