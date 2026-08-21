package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Colony Sentinel", x = 162, y = 1074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 165, y = 1118, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 179, y = 1126, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 180, y = 1039, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 185, y = 1158, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 191, y = 1069, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 192, y = 1172, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2023, y = 449, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2037, y = 420, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2052, y = 547, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2054, y = 465, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2062, y = 571, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2067, y = 448, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2073, y = 549, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2075, y = 495, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2081, y = 404, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2087, y = 566, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2091, y = 513, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2097, y = 533, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2098, y = 480, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2109, y = 346, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2113, y = 464, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2127, y = 447, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2139, y = 550, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2142, y = 397, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2143, y = 377, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2147, y = 480, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2158, y = 513, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2161, y = 548, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2163, y = 500, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2165, y = 530, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2173, y = 554, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2178, y = 414, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2188, y = 389, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2213, y = 378, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2213, y = 454, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2217, y = 412, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2224, y = 511, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 223, y = 1225, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2235, y = 467, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2238, y = 490, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2239, y = 479, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2261, y = 394, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2278, y = 317, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 228, y = 1112, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2287, y = 372, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2298, y = 386, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2303, y = 406, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2304, y = 342, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2330, y = 337, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 2332, y = 349, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 249, y = 1213, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 257, y = 1026, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 274, y = 1104, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 276, y = 1053, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 283, y = 1206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 287, y = 1129, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 289, y = 1060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 290, y = 1079, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 292, y = 1004, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 292, y = 1198, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 293, y = 1187, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 302, y = 1167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 309, y = 1181, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 312, y = 1213, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 316, y = 1135, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 318, y = 1077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 328, y = 998, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 329, y = 976, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 330, y = 1145, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 342, y = 1142, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 345, y = 1202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 346, y = 1168, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 350, y = 1060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 371, y = 1136, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 377, y = 1168, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 383, y = 1037, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 385, y = 1015, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 389, y = 1060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 401, y = 1011, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 401, y = 1112, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 415, y = 1167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 428, y = 1060, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 430, y = 1186, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 451, y = 1230, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 469, y = 1249, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 471, y = 1202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 475, y = 1314, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 506, y = 1178, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 508, y = 1205, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Colony Sentinel", x = 525, y = 1239, z = 0, stationary = false, aggressive = true)
public final class ColonySentinel extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public static final String CANONICAL_NAME = "Colony Sentinel";

  public ColonySentinel(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Colony Sentinel",
        "${monster.colony_sentinel}",
        1058,
        0,
        5,
        3000,
        54,
        122,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        84,
        258,
        java.util.List.of(
            new MonsterDef.LootDrop("Wasp wax", 0.005f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Finely cut aquamarine", 5.0E-4f),
            new MonsterDef.LootDrop("Finely cut sapphire", 0.002f),
            new MonsterDef.LootDrop("Finely cut amethyst", 0.01f)),
        false,
        0.0f,
        62,
        57,
        57,
        71,
        0,
        57,
        0,
        new int[] {47, 95, 71, 71, 71, 5000, 100, 100, 100, 100, 100, 100},
        47,
        198,
        0,
        1077346304,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        32,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d69+53", 574, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 80, 10091, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10314, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
