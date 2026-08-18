package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Gangrene Carrier", x = 1002, y = 2575, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1007, y = 2669, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1014, y = 2679, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1023, y = 2587, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1028, y = 2684, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1034, y = 2706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1050, y = 2627, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1052, y = 2558, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1070, y = 2706, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1072, y = 2668, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1086, y = 2589, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1092, y = 2545, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1109, y = 2593, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1109, y = 2699, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1132, y = 2673, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1149, y = 2563, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1156, y = 2592, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1156, y = 2836, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1157, y = 2640, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1167, y = 2690, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1169, y = 2611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1173, y = 2843, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1179, y = 2839, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1181, y = 2702, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1187, y = 2845, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1194, y = 2692, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1250, y = 2852, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1251, y = 2855, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1256, y = 2649, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1265, y = 2671, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1265, y = 2795, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1302, y = 2811, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1304, y = 2832, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1310, y = 2823, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1351, y = 2783, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1353, y = 2763, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1362, y = 2820, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1364, y = 2775, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1396, y = 2858, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1405, y = 2796, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1437, y = 2855, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1457, y = 2829, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1471, y = 2804, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1496, y = 2808, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 1523, y = 2781, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Gangrene Carrier", x = 999, y = 2621, z = 2, stationary = false, aggressive = true)
public final class GangreneCarrier extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Zombie Dying.wav";
  public static final String SOUND_HIT = "Zombie Hit.wav";

  public static final String CANONICAL_NAME = "Gangrene Carrier";

  public GangreneCarrier(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Gangrene Carrier",
        "${monster.gangrene_carrier}",
        2301,
        0,
        8,
        9409,
        108,
        245,
        30000L,
        "Zombie#h",
        "ZombieA#g",
        "ZombieC#j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        143,
        440,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough agate", 0.02f),
            new MonsterDef.LootDrop("Rough diamond", 0.004f),
            new MonsterDef.LootDrop("Rough moonstone", 0.001f),
            new MonsterDef.LootDrop("Garb of the dead", 0.005f)),
        false,
        0.0f,
        95,
        86,
        86,
        111,
        0,
        86,
        0,
        new int[] {67, 67, 90, 45, 5025, 56, 100, 100, 100, 100, 100, 100},
        80,
        330,
        0,
        1078198272,
        20009,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        37,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d138+107", 970, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 3, 10378, 1, 10),
            new MonsterDef.Attack("", 0, 5, 10379, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10376, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10119, 1, 10),
            new MonsterDef.Attack("", 0, 30, 10090, 1, 10),
            new MonsterDef.Attack("", 0, 3, 10358, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
