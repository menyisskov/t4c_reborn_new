package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Bandaged Horror", x = 411, y = 1747, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 428, y = 1829, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 435, y = 1849, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 441, y = 2429, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 446, y = 1822, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 479, y = 2409, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 481, y = 2388, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 495, y = 2387, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 496, y = 1730, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 506, y = 2319, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 511, y = 2339, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 513, y = 1713, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 519, y = 1753, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 523, y = 2313, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 525, y = 1718, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 530, y = 2363, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 531, y = 1768, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 540, y = 2419, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 544, y = 1898, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 548, y = 1807, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 550, y = 1735, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 552, y = 1776, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 552, y = 1886, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 555, y = 1931, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 555, y = 2335, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 557, y = 1823, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 565, y = 1956, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 566, y = 2365, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 567, y = 1798, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 573, y = 2339, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 574, y = 1858, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 582, y = 1750, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 583, y = 1849, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 583, y = 2352, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 585, y = 1937, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 586, y = 1901, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 589, y = 1877, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 590, y = 1737, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 593, y = 1928, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 593, y = 2311, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 595, y = 1779, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 604, y = 2322, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 611, y = 1756, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 679, y = 2425, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 702, y = 2446, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 715, y = 2416, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Bandaged Horror", x = 725, y = 2421, z = 1, stationary = false, aggressive = true)
public final class BandagedHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Bandaged Horror";

  public BandagedHorror(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Bandaged Horror",
        "${monster.bandaged_horror}",
        966,
        0,
        5,
        2574,
        48,
        109,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        79,
        242,
        java.util.List.of(
            new MonsterDef.LootDrop("Potion of fury", 0.05f),
            new MonsterDef.LootDrop("Mummy bandages", 0.03f),
            new MonsterDef.LootDrop("Gleaming shard", 0.02f)),
        false,
        0.0f,
        59,
        54,
        54,
        67,
        0,
        54,
        0,
        new int[] {73, 73, 97, 48, 5025, 48, 100, 100, 100, 100, 100, 100},
        44,
        186,
        0,
        1077280768,
        20011,
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
        java.util.List.of(new MonsterDef.Attack("1d62+47", 538, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
