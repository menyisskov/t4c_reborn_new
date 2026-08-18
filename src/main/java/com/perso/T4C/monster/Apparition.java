package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Apparition", x = 1920, y = 2011, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1931, y = 1998, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1932, y = 2001, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1939, y = 1958, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1947, y = 1993, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1949, y = 1970, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1951, y = 1952, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1953, y = 1983, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1954, y = 1940, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1959, y = 1967, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1963, y = 1982, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1967, y = 1943, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1968, y = 1965, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1970, y = 1990, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1976, y = 1950, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1978, y = 1959, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1983, y = 1988, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1984, y = 1976, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1992, y = 1942, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1993, y = 1965, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Apparition", x = 1996, y = 1935, z = 1, stationary = false, aggressive = true)
public final class Apparition extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Apparition";

  public Apparition(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Apparition",
        "${monster.apparition}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "",
        null,
        null,
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        98,
        302,
        java.util.List.of(),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {68, 68, 68, 68, 90, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        21042,
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
        java.util.List.of(new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
