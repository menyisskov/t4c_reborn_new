package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Draconis Knight", x = 1576, y = 1978, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1591, y = 1981, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1620, y = 2055, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1628, y = 2012, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1636, y = 2089, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1641, y = 1998, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1660, y = 1939, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1668, y = 1931, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1711, y = 1960, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1733, y = 1975, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1749, y = 2093, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1756, y = 2025, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1775, y = 2046, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1791, y = 2206, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1798, y = 2227, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1807, y = 2181, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1813, y = 2238, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1816, y = 2153, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1834, y = 2137, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1839, y = 2189, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1848, y = 2243, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1850, y = 2113, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1865, y = 2270, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1873, y = 2183, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1874, y = 2097, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1876, y = 2296, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1886, y = 2208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1891, y = 2306, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1899, y = 2073, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1911, y = 2297, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1922, y = 2157, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1926, y = 2063, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1926, y = 2316, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1932, y = 2028, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1935, y = 2325, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1940, y = 2274, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1941, y = 2042, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1949, y = 2147, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1952, y = 2329, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1971, y = 2315, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1979, y = 2336, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 1980, y = 2290, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 2012, y = 2209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 2024, y = 2290, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 2045, y = 2157, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Draconis Knight", x = 2074, y = 2302, z = 1, stationary = false, aggressive = true)
public final class DraconisKnight extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 3.wav";
  public static final String SOUND_DEATH = "Demon Dying.wav";
  public static final String SOUND_HIT = "Demon Hit.wav";

  public static final String CANONICAL_NAME = "Draconis Knight";

  public DraconisKnight(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Draconis Knight",
        "${monster.draconis_knight}",
        1961,
        0,
        7,
        7198,
        88,
        201,
        30000L,
        "MonsDraconianPlate#k",
        "MonsDraconianPlateA#k",
        "MonsDraconianPlateC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        129,
        396,
        java.util.List.of(),
        false,
        0.0f,
        87,
        79,
        79,
        101,
        0,
        79,
        0,
        new int[] {80, 80, 80, 80, 80, 5000, 100, 100, 100, 100, 100, 100},
        72,
        298,
        0,
        1078067200,
        20065,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d114+87", 874, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
