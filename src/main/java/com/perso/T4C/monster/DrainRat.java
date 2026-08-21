package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Drain Rat", x = 2727, y = 1951, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2732, y = 2003, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2735, y = 2047, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2746, y = 2023, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2749, y = 1988, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2755, y = 1965, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2756, y = 1981, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2756, y = 2018, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2758, y = 2044, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2764, y = 1960, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2778, y = 1974, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2779, y = 2035, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2784, y = 2062, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2794, y = 1960, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2797, y = 1939, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2802, y = 2041, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2803, y = 2019, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2804, y = 1965, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2805, y = 1990, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2810, y = 2024, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2822, y = 2002, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Rat", x = 2836, y = 1983, z = 0, stationary = false, aggressive = true)
public final class DrainRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Drain Rat";

  public DrainRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Drain Rat",
        "${monster.drain_rat}",
        631,
        0,
        3,
        1052,
        28,
        43,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Ring of the forester", 0.004f),
            new MonsterDef.LootDrop("Drachenstaff", 0.005f),
            new MonsterDef.LootDrop("Raindrop", 0.02f),
            new MonsterDef.LootDrop("Scroll of minor combat sense", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.02f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {106, 53, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1077477376,
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
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d16+27", 370, 50, 0, 0, 1),
            new MonsterDef.Attack("", 0, 50, 10648, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
