package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Goblin", x = 135, y = 604, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 171, y = 1002, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 184, y = 576, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 191, y = 583, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2077, y = 1165, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2083, y = 1167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 209, y = 631, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2242, y = 1295, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2252, y = 1287, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2253, y = 1296, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2317, y = 1187, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2319, y = 1190, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2323, y = 1178, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2329, y = 1197, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2330, y = 1197, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2331, y = 1185, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2335, y = 1194, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2336, y = 1194, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 240, y = 1152, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2476, y = 1098, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2532, y = 1148, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2533, y = 1172, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2537, y = 1139, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2539, y = 1166, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2539, y = 1189, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2543, y = 1132, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2550, y = 1170, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2559, y = 1137, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2560, y = 1141, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2570, y = 1146, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2597, y = 1061, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2598, y = 1075, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2603, y = 1050, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2621, y = 1049, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2623, y = 1286, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2625, y = 1039, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2630, y = 1285, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2630, y = 1296, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2638, y = 1036, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2638, y = 1290, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2639, y = 1290, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2641, y = 1134, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2642, y = 1133, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2642, y = 1298, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2649, y = 1150, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2651, y = 1139, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2653, y = 1131, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2665, y = 1214, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2669, y = 1204, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2676, y = 1150, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2676, y = 1212, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2678, y = 1206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2678, y = 961, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 270, y = 1135, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2712, y = 1003, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2727, y = 1009, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2739, y = 1009, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 274, y = 1061, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2741, y = 1141, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2745, y = 1189, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2750, y = 1159, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2752, y = 1015, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2758, y = 1137, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2759, y = 1010, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2762, y = 997, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2767, y = 1014, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2768, y = 996, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2769, y = 1014, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2770, y = 1007, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2771, y = 970, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2772, y = 1000, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2776, y = 1017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2777, y = 1212, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2778, y = 1004, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2780, y = 966, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2785, y = 996, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2790, y = 1031, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2790, y = 1035, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 282, y = 635, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 2846, y = 1253, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 290, y = 403, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 291, y = 678, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 301, y = 335, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 306, y = 541, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 324, y = 1109, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 331, y = 608, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 352, y = 560, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 378, y = 667, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 413, y = 1034, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 437, y = 325, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 472, y = 288, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 488, y = 603, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 512, y = 453, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 524, y = 407, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 542, y = 412, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 604, y = 528, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 71, y = 283, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 74, y = 257, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 92, y = 301, z = 1, stationary = false, aggressive = true)
@Spawn(type = "Goblin", x = 93, y = 272, z = 1, stationary = false, aggressive = true)
public final class Goblin extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 2.wav";
  public static final String SOUND_DEATH = "Goblin Dying.wav";
  public static final String SOUND_HIT = "Goblin Hit.wav";

  public static final String CANONICAL_NAME = "Goblin";

  public Goblin(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Goblin",
        "${monster.goblin}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Goblin#l",
        "GoblinA#i",
        "GoblinC!o",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        8,
        27,
        java.util.List.of(
            new MonsterDef.LootDrop("Goblin leather armor", 0.005f),
            new MonsterDef.LootDrop("Iron ring", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.06f),
            new MonsterDef.LootDrop("Healing potion", 0.02f)),
        false,
        0.0f,
        20,
        19,
        19,
        21,
        0,
        19,
        0,
        new int[] {121, 60, 91, 91, 91, 5025, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
        20001,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
