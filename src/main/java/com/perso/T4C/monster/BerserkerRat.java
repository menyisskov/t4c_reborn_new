package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Berserker Rat", x = 1456, y = 501, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1457, y = 489, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1469, y = 498, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1474, y = 489, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1478, y = 510, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1487, y = 461, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1490, y = 501, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1492, y = 488, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1507, y = 484, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1508, y = 412, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1510, y = 436, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1512, y = 395, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1519, y = 415, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1524, y = 385, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1535, y = 428, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1551, y = 445, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Berserker Rat", x = 1556, y = 387, z = 2, stationary = false, aggressive = true)
public final class BerserkerRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Berserker Rat";

  public BerserkerRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Berserker Rat",
        "${monster.berserker_rat}",
        1571,
        0,
        6,
        5224,
        73,
        166,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        111,
        341,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of nimbleness", 0.05f),
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f)),
        false,
        0.0f,
        77,
        70,
        70,
        89,
        0,
        70,
        0,
        new int[] {103, 51, 77, 77, 77, 5000, 100, 100, 100, 100, 100, 100},
        62,
        258,
        0,
        1077870592,
        20003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        43,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d94+72", 754, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10352, 2, 8),
            new MonsterDef.Attack("", 0, 3, 10351, 2, 8),
            new MonsterDef.Attack("", 0, 10, 10355, 9, 18)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
