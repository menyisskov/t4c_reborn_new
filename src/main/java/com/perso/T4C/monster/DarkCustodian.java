package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Dark Custodian", x = 2663, y = 1957, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2664, y = 1989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2668, y = 2020, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2680, y = 1929, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2682, y = 2065, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2699, y = 1963, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2699, y = 2020, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2712, y = 1952, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2720, y = 2051, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2722, y = 1907, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2737, y = 2097, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2752, y = 2076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2758, y = 1919, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2771, y = 2099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2798, y = 2069, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2811, y = 1946, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2822, y = 1909, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2822, y = 2099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2830, y = 1938, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2848, y = 2029, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2849, y = 2014, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2849, y = 2060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2851, y = 2022, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2857, y = 2029, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2859, y = 1954, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2859, y = 1997, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2859, y = 2003, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2870, y = 2028, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2871, y = 2011, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2874, y = 2081, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2876, y = 2017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2889, y = 1948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2895, y = 2067, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Dark Custodian", x = 2900, y = 1998, z = 0, stationary = false, aggressive = true)
public final class DarkCustodian extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public static final String CANONICAL_NAME = "Dark Custodian";

  public DarkCustodian(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Dark Custodian",
        "${monster.dark_custodian}",
        1153,
        0,
        5,
        3432,
        60,
        135,
        30000L,
        "TreeEnt#i",
        "TreeEntA#i",
        "TreeEntC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachensword", 0.003f),
            new MonsterDef.LootDrop("Serious healing potion", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Scroll of resist fire", 0.01f),
            new MonsterDef.LootDrop("Scroll of resist ice", 0.01f),
            new MonsterDef.LootDrop("Scroll of protection", 0.01f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {93, 93, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        50,
        75,
        0,
        1079083008,
        20018,
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
            new MonsterDef.Attack("1d76+59", 520, 90, 0, 0, 1),
            new MonsterDef.Attack("", 0, 25, 10653, 2, 15),
            new MonsterDef.Attack("", 0, 65, 10094, 2, 15),
            new MonsterDef.Attack("", 0, 10, 10628, 0, 1)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
