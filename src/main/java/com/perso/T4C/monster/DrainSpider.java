package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Drain Spider", x = 2669, y = 1948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2673, y = 2005, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2679, y = 1979, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2688, y = 2053, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2698, y = 1982, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2700, y = 2031, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2709, y = 2005, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2710, y = 1945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2711, y = 2043, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2718, y = 1938, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2719, y = 2090, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2727, y = 2054, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2729, y = 1913, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2740, y = 1935, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2746, y = 1923, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2752, y = 2064, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2772, y = 2086, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2778, y = 1935, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2781, y = 2096, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2789, y = 1917, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2792, y = 2076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2805, y = 2088, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2809, y = 1927, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2829, y = 2093, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2837, y = 2068, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2842, y = 2073, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2845, y = 2049, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2848, y = 1946, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2853, y = 2084, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2855, y = 2045, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2862, y = 1974, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2862, y = 2013, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2872, y = 1951, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2878, y = 2036, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Drain Spider", x = 2882, y = 1983, z = 0, stationary = false, aggressive = true)
public final class DrainSpider extends DataMonster {
  public static final String SOUND_ATTACK = "Spider Attack.wav";
  public static final String SOUND_DEATH = "Spider Dying.wav";
  public static final String SOUND_HIT = "Spider Hit.wav";

  public static final String CANONICAL_NAME = "Drain Spider";

  public DrainSpider(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Drain Spider",
        "${monster.drain_spider}",
        1153,
        0,
        6,
        3462,
        60,
        135,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        89,
        275,
        java.util.List.of(
            new MonsterDef.LootDrop("Drachenrobe", 0.005f),
            new MonsterDef.LootDrop("Bow of the Spiders", 0.02f),
            new MonsterDef.LootDrop("Raincloak", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Mana elixir", 0.02f),
            new MonsterDef.LootDrop("Scroll of orientation middle", 0.01f)),
        false,
        0.0f,
        65,
        59,
        59,
        75,
        0,
        59,
        0,
        new int[] {93, 46, 70, 70, 70, 5000, 100, 100, 100, 100, 100, 100},
        50,
        210,
        0,
        1077477376,
        20007,
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
            new MonsterDef.Attack("1d76+59", 610, 75, 0, 0, 1),
            new MonsterDef.Attack("", 0, 25, 10654, 0, 1),
            new MonsterDef.Attack("", 0, 10, 10652, 2, 15),
            new MonsterDef.Attack("", 0, 10, 10628, 2, 15),
            new MonsterDef.Attack("", 0, 80, 10091, 2, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
