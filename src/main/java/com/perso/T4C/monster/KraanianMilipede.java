package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Milipede", x = 101, y = 1356, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 102, y = 1472, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 111, y = 1476, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 156, y = 1358, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 163, y = 1458, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 166, y = 1343, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 170, y = 1546, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1729, y = 901, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1751, y = 782, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1765, y = 755, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1769, y = 885, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 177, y = 1369, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1778, y = 929, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1785, y = 798, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1806, y = 766, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1809, y = 673, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1810, y = 772, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1816, y = 905, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1862, y = 686, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1890, y = 949, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1891, y = 830, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1892, y = 933, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1931, y = 937, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1935, y = 825, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1947, y = 760, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1950, y = 807, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1958, y = 796, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1972, y = 695, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1990, y = 945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1991, y = 712, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 1995, y = 663, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2006, y = 684, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2009, y = 781, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2040, y = 951, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2046, y = 621, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2049, y = 954, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2059, y = 892, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2074, y = 635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2092, y = 914, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2093, y = 902, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2102, y = 751, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2109, y = 759, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2111, y = 817, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2125, y = 822, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2140, y = 785, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2152, y = 649, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 22, y = 1456, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2209, y = 636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2223, y = 711, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 2295, y = 810, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 283, y = 1279, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 287, y = 1441, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 289, y = 1279, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 290, y = 1274, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 290, y = 1500, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 291, y = 1438, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 292, y = 1503, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 305, y = 1262, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 307, y = 1268, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 327, y = 1390, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 376, y = 1420, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 377, y = 1422, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 389, y = 1378, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 404, y = 1487, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 406, y = 1303, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 411, y = 1351, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 412, y = 1425, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 429, y = 1366, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 45, y = 1426, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 462, y = 1303, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 467, y = 1425, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 468, y = 1420, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 486, y = 1494, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 488, y = 1206, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 500, y = 1400, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 504, y = 1435, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 506, y = 1313, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 516, y = 1477, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 517, y = 1281, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 520, y = 1477, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 523, y = 1476, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 544, y = 1230, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 567, y = 1344, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 572, y = 1352, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 588, y = 1307, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 591, y = 1309, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 598, y = 1245, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 598, y = 1258, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 60, y = 1422, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 600, y = 1314, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 600, y = 1350, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 70, y = 1464, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Milipede", x = 98, y = 1351, z = 2, stationary = false, aggressive = true)
@Spawn(type = "KraanianMilipede", x = 2295, y = 768, z = 0, stationary = false, aggressive = true)
@Spawn(type = "KraanianMilipede", x = 2297, y = 815, z = 0, stationary = false, aggressive = true)
@Spawn(type = "KraanianMilipede", x = 2299, y = 768, z = 0, stationary = false, aggressive = true)
@Spawn(type = "KraanianMilipede", x = 2316, y = 826, z = 0, stationary = false, aggressive = true)
public final class KraanianMilipede extends DataMonster {
  public static final String SOUND_ATTACK = "Atrocity Attack.wav";
  public static final String SOUND_DEATH = "Atrocity Dying.wav";
  public static final String SOUND_HIT = "Atrocity Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Milipede";

  public KraanianMilipede(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Milipede",
        "${monster.kraanian_milipede}",
        313,
        0,
        2,
        453,
        15,
        35,
        30000L,
        "KraanianMilipede#i",
        "KraanianMilipedeA#h",
        "KraanianMilipedeC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        32,
        99,
        java.util.List.of(new MonsterDef.LootDrop("Kraanian egg", 0.02f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {113, 56, 85, 85, 85, 5000, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        20035,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        18,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
