package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Mysticist", x = 2578, y = 2013, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2588, y = 1934, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2611, y = 1933, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2612, y = 1985, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2631, y = 2031, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2636, y = 1902, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2636, y = 2016, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2640, y = 1948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2651, y = 1901, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2655, y = 2094, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2656, y = 2050, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2660, y = 2078, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2680, y = 1971, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2692, y = 1916, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2692, y = 2109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2706, y = 2072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2713, y = 2109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2739, y = 1892, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2768, y = 2122, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2779, y = 1889, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2823, y = 1886, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2828, y = 2124, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2860, y = 1925, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2867, y = 2125, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2869, y = 1906, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2888, y = 2011, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2890, y = 2111, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2907, y = 2108, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2912, y = 2059, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2933, y = 2011, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2943, y = 1921, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2954, y = 1945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Mysticist", x = 2954, y = 1971, z = 0, stationary = false, aggressive = true)
public final class r154Mysticist extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r154Mysticist(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Mysticist",
        "${monster.mysticist}",
        3011,
        0,
        3,
        5950,
        30,
        90,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        134,
        412,
        java.util.List.of(
            new MonsterDef.LootDrop("Escape scroll", 0.008f),
            new MonsterDef.LootDrop("Drachenshield", 0.004f),
            new MonsterDef.LootDrop("Cloak of the Skull", 0.02f),
            new MonsterDef.LootDrop("Manastone", 0.01f),
            new MonsterDef.LootDrop("Scroll of detect hidden", 0.01f),
            new MonsterDef.LootDrop("Scroll of detect invisible", 0.01f),
            new MonsterDef.LootDrop("Scroll of earthen strength", 0.01f),
            new MonsterDef.LootDrop("Scroll of orientation middle", 0.03f)),
        false,
        0.0f,
        90,
        82,
        82,
        105,
        0,
        82,
        0,
        new int[] {58, 58, 58, 58, 78, 5000, 100, 100, 100, 100, 100, 100},
        75,
        500,
        0,
        1077805056,
        10011,
        41148,
        0,
        0,
        0,
        0,
        40672,
        0,
        0,
        100,
        35,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d61+29", 750, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10652, 2, 15),
            new MonsterDef.Attack("", 0, 90, 10094, 2, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
