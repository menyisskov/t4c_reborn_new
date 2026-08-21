package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Taunting Horror", x = 186, y = 2076, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 195, y = 2077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 195, y = 2095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 197, y = 2061, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 205, y = 2095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 206, y = 2111, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 214, y = 2156, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 215, y = 2006, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 215, y = 2131, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 219, y = 2013, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 220, y = 2233, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 225, y = 2000, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 225, y = 2169, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 226, y = 2236, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 228, y = 2121, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 229, y = 2191, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 229, y = 2212, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 233, y = 2263, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 240, y = 2110, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 241, y = 1988, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 242, y = 2174, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 246, y = 2222, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 253, y = 1999, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 254, y = 2017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 267, y = 1992, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 272, y = 1944, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 272, y = 1969, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 273, y = 2016, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 274, y = 1992, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 283, y = 1980, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 284, y = 2029, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 302, y = 1965, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 321, y = 2031, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 325, y = 1931, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 332, y = 1911, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 334, y = 1891, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 344, y = 1945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 346, y = 2186, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 353, y = 2048, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 362, y = 1899, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 365, y = 1979, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 366, y = 1881, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 367, y = 2195, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 376, y = 2017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 380, y = 2101, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 381, y = 2057, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 385, y = 1961, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 388, y = 2247, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 389, y = 2132, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 389, y = 2186, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 389, y = 2297, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 393, y = 1927, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 398, y = 2117, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 409, y = 2202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 411, y = 1873, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 411, y = 1903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 414, y = 2144, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 414, y = 2323, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 416, y = 2083, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 418, y = 2195, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 419, y = 1920, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 422, y = 2211, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 423, y = 2006, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 427, y = 2091, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 429, y = 2012, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 431, y = 2261, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 432, y = 1948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 439, y = 2292, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 441, y = 1926, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 443, y = 1857, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 443, y = 2321, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 447, y = 2067, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 450, y = 2081, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 451, y = 1830, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 454, y = 1842, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 457, y = 2163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 461, y = 1968, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 468, y = 2317, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 470, y = 2156, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 473, y = 2072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 477, y = 2101, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 479, y = 2112, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 479, y = 2241, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 480, y = 2207, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 492, y = 2302, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 497, y = 2123, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 501, y = 1924, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 502, y = 2338, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 507, y = 2173, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 507, y = 2269, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 516, y = 1957, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 524, y = 2333, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 525, y = 2193, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 530, y = 1948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 532, y = 1936, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 535, y = 1981, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 539, y = 2279, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 544, y = 1997, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 544, y = 2041, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 544, y = 2235, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 550, y = 1941, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 551, y = 2258, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 554, y = 1972, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 554, y = 2205, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 557, y = 2301, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 571, y = 2349, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 574, y = 2285, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 575, y = 2109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 579, y = 2343, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 582, y = 2304, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 583, y = 2378, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 585, y = 2209, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 586, y = 2058, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 587, y = 2074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 593, y = 2108, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 595, y = 2361, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 599, y = 2334, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 604, y = 2144, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 606, y = 2100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 613, y = 2244, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 617, y = 2127, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 623, y = 2082, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 627, y = 2144, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 627, y = 2163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 629, y = 2291, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 635, y = 2311, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 643, y = 2259, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 644, y = 2281, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 654, y = 2115, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 657, y = 2146, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 660, y = 2085, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 663, y = 2245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 670, y = 2209, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 685, y = 2193, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 690, y = 2113, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 690, y = 2328, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 692, y = 2203, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 701, y = 2278, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 703, y = 2218, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 704, y = 2258, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 706, y = 2309, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 706, y = 2324, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 709, y = 2245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 710, y = 2289, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 717, y = 2088, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 726, y = 2112, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 730, y = 2267, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 736, y = 2138, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 739, y = 2221, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 740, y = 2163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 745, y = 2193, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 745, y = 2208, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 748, y = 2254, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 756, y = 2221, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 765, y = 2260, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 797, y = 2256, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Taunting Horror", x = 804, y = 2274, z = 0, stationary = false, aggressive = true)
public final class r238TauntingHorror extends DataMonster {
  public static final String SOUND_ATTACK = "Taunting Attack.wav";
  public static final String SOUND_DEATH = "Taunting Dying.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public r238TauntingHorror(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Taunting Horror",
        "${monster.taunting_horror}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC!m",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        53,
        165,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Torch", 0.02f),
            new MonsterDef.LootDrop("Sapphire bracelet", 0.01f),
            new MonsterDef.LootDrop("Potion of fury", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.01f)),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {53, 106, 79, 79, 79, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        20038,
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
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
