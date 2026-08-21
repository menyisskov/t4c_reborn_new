package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Kraanian Flyer", x = 107, y = 1543, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 110, y = 1339, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 125, y = 1282, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 126, y = 1558, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 131, y = 1538, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 133, y = 1306, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 133, y = 1556, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 174, y = 1303, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1743, y = 832, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1745, y = 846, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1749, y = 842, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1767, y = 676, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1776, y = 701, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1804, y = 751, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1806, y = 744, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1826, y = 949, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1830, y = 938, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 189, y = 1289, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1893, y = 660, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1908, y = 670, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1946, y = 948, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1951, y = 942, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1952, y = 945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 1984, y = 905, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2075, y = 811, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2085, y = 817, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2092, y = 633, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2094, y = 639, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2097, y = 633, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2106, y = 723, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2112, y = 707, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2112, y = 717, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2121, y = 748, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2135, y = 745, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2202, y = 671, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2207, y = 674, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2211, y = 671, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2262, y = 678, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2272, y = 682, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2286, y = 773, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2299, y = 779, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 2303, y = 799, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 236, y = 1301, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 248, y = 1388, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 253, y = 1585, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 256, y = 1321, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 256, y = 1395, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 264, y = 1390, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 291, y = 1286, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 295, y = 1322, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 298, y = 1366, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 298, y = 1527, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 302, y = 1581, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 310, y = 1329, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 336, y = 1198, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 342, y = 1252, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 347, y = 1584, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 35, y = 1470, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 35, y = 1497, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 356, y = 1417, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 368, y = 1290, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 368, y = 1510, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 370, y = 1217, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 376, y = 1213, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 389, y = 1191, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 423, y = 1509, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 43, y = 1525, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 430, y = 1224, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 439, y = 1499, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 453, y = 1457, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 456, y = 1454, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 47, y = 1475, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 476, y = 1234, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 520, y = 1460, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 526, y = 1458, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 554, y = 1268, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 563, y = 1329, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 587, y = 1236, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 63, y = 1532, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 87, y = 1524, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Kraanian Flyer", x = 90, y = 1459, z = 2, stationary = false, aggressive = true)
@Spawn(type = "KraanianFlying", x = 2282, y = 763, z = 0, stationary = false, aggressive = true)
@Spawn(type = "KraanianFlying", x = 2287, y = 739, z = 0, stationary = false, aggressive = true)
@Spawn(type = "KraanianFlying", x = 2314, y = 776, z = 0, stationary = false, aggressive = true)
public final class KraanianFlyer extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Flyer";

  public KraanianFlyer(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Flyer",
        "${monster.kraanian_flyer}",
        293,
        0,
        2,
        413,
        14,
        33,
        30000L,
        "KraanianFlying#h",
        "KraanianFlyingA#h",
        "KraanianFlyingC!l",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        30,
        93,
        java.util.List.of(),
        false,
        0.0f,
        32,
        30,
        30,
        35,
        0,
        30,
        0,
        new int[] {57, 114, 86, 86, 86, 5000, 100, 100, 100, 100, 100, 100},
        17,
        118,
        0,
        0,
        20034,
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
        java.util.List.of(
            new MonsterDef.Attack("1d20+13", 214, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 1, 20)),
        false,
        0,
        java.util.List.of("KraanianFlying"),
        java.util.Map.of());
  }
}
