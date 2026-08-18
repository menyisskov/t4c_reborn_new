package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.monster.core.NamedEventMonster;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "MOBYOGGOTHWORM", x = 1005, y = 352, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1012, y = 295, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1012, y = 389, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1013, y = 308, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1013, y = 373, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1014, y = 250, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1015, y = 333, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1016, y = 358, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1019, y = 228, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1019, y = 265, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1021, y = 245, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1029, y = 205, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1032, y = 363, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1033, y = 377, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1037, y = 249, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1037, y = 321, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1041, y = 146, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1042, y = 181, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1045, y = 125, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1045, y = 196, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1046, y = 202, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1048, y = 221, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1050, y = 253, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1054, y = 149, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1057, y = 171, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1059, y = 163, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1060, y = 115, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1061, y = 342, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1062, y = 184, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1064, y = 109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1065, y = 129, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1065, y = 143, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1066, y = 323, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1067, y = 362, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1070, y = 217, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1077, y = 237, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1077, y = 270, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1078, y = 195, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1078, y = 88, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1079, y = 97, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1080, y = 256, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1084, y = 129, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1090, y = 222, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1092, y = 297, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1092, y = 330, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1092, y = 353, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1094, y = 122, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1098, y = 150, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1107, y = 319, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1110, y = 190, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1110, y = 207, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1110, y = 291, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1111, y = 107, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1111, y = 349, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1115, y = 308, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1116, y = 359, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1121, y = 108, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1121, y = 162, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1123, y = 351, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1127, y = 242, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1128, y = 365, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1132, y = 223, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1133, y = 171, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1133, y = 327, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1134, y = 208, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1135, y = 133, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1135, y = 148, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1136, y = 111, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1136, y = 94, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1137, y = 350, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1138, y = 282, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1139, y = 294, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1143, y = 317, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1144, y = 266, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1146, y = 178, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1149, y = 185, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1150, y = 196, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1150, y = 209, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1152, y = 132, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1154, y = 158, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1157, y = 225, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1159, y = 347, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1159, y = 365, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1160, y = 199, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1161, y = 109, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1164, y = 146, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1164, y = 188, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1165, y = 274, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1169, y = 303, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1172, y = 292, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1175, y = 331, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1184, y = 348, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 1192, y = 336, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2024, y = 1102, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2032, y = 1147, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2058, y = 1123, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2073, y = 1164, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2080, y = 1119, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2088, y = 1142, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2090, y = 1162, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2091, y = 1094, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2107, y = 1074, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2121, y = 1096, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2121, y = 1123, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2124, y = 999, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2130, y = 1021, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2130, y = 1120, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2135, y = 1016, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2137, y = 1083, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2171, y = 984, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2173, y = 995, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2192, y = 1065, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2215, y = 1078, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2237, y = 1067, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2246, y = 1009, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2299, y = 1126, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2306, y = 1104, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2312, y = 1134, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2331, y = 1081, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2348, y = 1132, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2350, y = 1044, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2367, y = 1026, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2374, y = 1108, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2377, y = 1018, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2379, y = 1125, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2381, y = 1085, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2384, y = 1026, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2387, y = 1136, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2665, y = 166, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2677, y = 208, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2695, y = 228, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2700, y = 111, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2723, y = 238, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2730, y = 266, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2742, y = 333, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2754, y = 284, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2759, y = 199, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2764, y = 148, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2778, y = 221, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2795, y = 203, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2796, y = 308, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2806, y = 251, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2832, y = 291, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2835, y = 138, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2848, y = 255, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2888, y = 274, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2893, y = 336, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2907, y = 130, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2917, y = 417, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2923, y = 218, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2923, y = 368, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2931, y = 245, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2937, y = 162, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2948, y = 260, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2977, y = 221, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 2986, y = 251, z = 2, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 911, y = 271, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 911, y = 301, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 916, y = 323, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 916, y = 339, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 921, y = 300, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 921, y = 355, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 926, y = 351, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 932, y = 287, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 934, y = 328, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 937, y = 367, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 943, y = 381, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 946, y = 283, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 950, y = 336, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 954, y = 347, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 956, y = 386, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 957, y = 276, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 967, y = 368, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 971, y = 389, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 978, y = 256, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 979, y = 272, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 979, y = 373, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 980, y = 351, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 982, y = 288, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 987, y = 394, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 996, y = 266, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 996, y = 297, z = 0, stationary = false, aggressive = true)
@Spawn(type = "MOBYOGGOTHWORM", x = 997, y = 258, z = 0, stationary = false, aggressive = true)
public final class YoggothWorm extends NamedEventMonster {
  public static final String SOUND_ATTACK = "Worm Attack.wav";
  public static final String SOUND_DEATH = "Worm Dying.wav";
  public static final String SOUND_HIT = "Worm Hit.wav";

  public YoggothWorm(MonsterDef d, float x, float y) throws GameException {

    super(d, x, y);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Yoggoth Worm",
        "${monster.yoggoth_worm}",
        1497,
        0,
        6,
        4869,
        0,
        0,
        30000L,
        "SmallWorm#m",
        "SmallWormA#k",
        "SmallWormC#k",
        "Worm Attack.wav",
        "Worm Dying.wav",
        "Worm Hit.wav",
        107,
        330,
        java.util.List.of(
            new MonsterDef.LootDrop("Rough amethyst", 0.02f),
            new MonsterDef.LootDrop("Rough sapphire", 0.004f),
            new MonsterDef.LootDrop("Rough aquamarine", 0.001f),
            new MonsterDef.LootDrop("Finely cut amethyst", 0.01f),
            new MonsterDef.LootDrop("Finely cut sapphire", 0.002f),
            new MonsterDef.LootDrop("Finely cut moonstone", 5.0E-4f)),
        false,
        0.0f,
        75,
        68,
        68,
        87,
        0,
        68,
        0,
        new int[] {78, 78, 52, 105, 78, 5000, 100, 100, 100, 100, 100, 100},
        60,
        250,
        0,
        1077805056,
        20016,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        41,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 97, 10091, 3, 10),
            new MonsterDef.Attack("1d90+69", 730, 100, 10091, 0, 2),
            new MonsterDef.Attack("", 0, 3, 10317, 3, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
