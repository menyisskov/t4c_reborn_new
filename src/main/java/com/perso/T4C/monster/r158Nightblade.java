package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Nightblade", x = 1006, y = 642, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1031, y = 671, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1051, y = 687, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1052, y = 661, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1087, y = 681, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1090, y = 689, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1096, y = 592, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1104, y = 647, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1114, y = 634, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1132, y = 589, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1149, y = 607, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1171, y = 628, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1325, y = 275, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1334, y = 267, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1344, y = 230, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1349, y = 160, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1349, y = 187, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1351, y = 141, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1354, y = 279, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1356, y = 174, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1358, y = 291, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1363, y = 307, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1365, y = 169, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1365, y = 266, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1366, y = 176, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1381, y = 302, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1388, y = 233, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1390, y = 204, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1396, y = 322, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1396, y = 327, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1399, y = 195, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1400, y = 246, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1401, y = 159, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1404, y = 304, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1408, y = 257, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1410, y = 298, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1414, y = 181, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1415, y = 337, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1416, y = 340, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1419, y = 128, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1431, y = 164, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1431, y = 166, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1434, y = 105, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1434, y = 54, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1443, y = 80, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1447, y = 35, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1449, y = 402, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1451, y = 38, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1454, y = 142, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1455, y = 63, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1456, y = 381, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1464, y = 383, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1472, y = 156, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1474, y = 427, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1475, y = 390, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1476, y = 87, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1478, y = 72, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 148, y = 2722, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1484, y = 148, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1484, y = 373, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1485, y = 415, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1486, y = 84, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1487, y = 404, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 149, y = 2726, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1492, y = 174, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1498, y = 109, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1505, y = 159, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1517, y = 172, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1522, y = 133, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1536, y = 172, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 154, y = 2717, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1544, y = 155, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1544, y = 85, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1549, y = 143, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1553, y = 248, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1556, y = 129, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1558, y = 253, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1559, y = 168, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1567, y = 152, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1572, y = 72, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1582, y = 221, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1593, y = 218, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 161, y = 2717, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1623, y = 208, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1624, y = 211, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 1630, y = 209, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 166, y = 2706, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 166, y = 2709, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 175, y = 2709, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 178, y = 2711, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 178, y = 2715, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 178, y = 2735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 182, y = 2721, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 187, y = 2723, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 205, y = 1844, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 207, y = 1834, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 208, y = 1846, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 209, y = 1825, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 212, y = 1852, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 213, y = 1846, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 227, y = 1834, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 230, y = 1835, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 230, y = 1840, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 268, y = 1729, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 270, y = 1657, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 270, y = 1735, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 272, y = 1657, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 275, y = 1730, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 277, y = 1732, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 278, y = 1650, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 278, y = 1713, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 278, y = 1723, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 280, y = 2794, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 281, y = 1723, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 283, y = 2797, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 285, y = 2790, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 287, y = 2789, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 288, y = 2794, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 288, y = 2798, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 290, y = 2794, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 290, y = 2795, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 420, y = 1729, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 424, y = 1848, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 430, y = 1737, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 433, y = 1725, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 433, y = 1745, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 443, y = 1742, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 444, y = 1733, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 488, y = 1625, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 490, y = 1621, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 494, y = 1615, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 494, y = 1629, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 506, y = 1638, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 511, y = 1627, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 560, y = 2506, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 565, y = 2514, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 566, y = 2529, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 566, y = 2530, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 568, y = 2509, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 571, y = 2523, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 571, y = 2534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 573, y = 2515, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 576, y = 2521, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 613, y = 1675, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 619, y = 2431, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 619, y = 2439, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 622, y = 2427, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 622, y = 2434, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 623, y = 2441, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 625, y = 1631, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 626, y = 1701, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 630, y = 2420, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 632, y = 2423, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 634, y = 1661, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 712, y = 1721, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 719, y = 1717, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 725, y = 1713, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 745, y = 2422, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 753, y = 2428, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 754, y = 2428, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 756, y = 2414, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 759, y = 2416, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 759, y = 2417, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 767, y = 2422, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 768, y = 2415, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 833, y = 1774, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 834, y = 1786, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 881, y = 793, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 885, y = 2745, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 885, y = 2764, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 892, y = 2759, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 893, y = 2764, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 898, y = 2757, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 901, y = 2745, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 901, y = 722, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 907, y = 812, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 928, y = 774, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 940, y = 691, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 956, y = 2394, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 963, y = 2268, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 965, y = 601, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 966, y = 694, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 968, y = 2267, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 968, y = 686, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 969, y = 2380, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 981, y = 2408, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 982, y = 638, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Nightblade", x = 995, y = 2385, z = 0, stationary = false, aggressive = true)
public final class r158Nightblade extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public r158Nightblade(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Nightblade",
        "${monster.nightblade}",
        710,
        0,
        4,
        1575,
        34,
        78,
        30000L,
        "Thief#m",
        "ThiefA#i",
        "ThiefC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        62,
        192,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Potion of mana", 0.02f),
            new MonsterDef.LootDrop("Polished short sword", 0.01f),
            new MonsterDef.LootDrop("Pouch of Blue Cohosh", 0.01f),
            new MonsterDef.LootDrop("Golden chalice", 0.008f)),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {77, 77, 77, 77, 51, 5000, 100, 100, 100, 100, 100, 100},
        35,
        150,
        0,
        1076953088,
        20042,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 42, 10096, 3, 13),
            new MonsterDef.Attack("", 0, 42, 10086, 3, 13),
            new MonsterDef.Attack("", 0, 8, 10122, 3, 13)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
