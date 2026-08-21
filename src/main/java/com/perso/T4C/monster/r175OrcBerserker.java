package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Orc Berserker", x = 1001, y = 1915, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1003, y = 2065, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1008, y = 1989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1009, y = 1222, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1012, y = 1940, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1016, y = 1307, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1018, y = 1218, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1019, y = 2143, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1020, y = 1979, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1025, y = 1244, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1028, y = 1224, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1030, y = 1296, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1032, y = 1012, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1033, y = 1961, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1035, y = 1018, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1035, y = 1283, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1037, y = 1045, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1040, y = 1102, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1044, y = 1343, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1044, y = 1879, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1045, y = 1922, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1048, y = 1931, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1049, y = 1092, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1050, y = 1069, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1050, y = 2013, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1053, y = 1106, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1057, y = 1338, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1061, y = 1097, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1061, y = 2083, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1064, y = 1076, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1065, y = 1315, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1070, y = 2107, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1071, y = 1302, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1071, y = 2017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1071, y = 2064, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1072, y = 1070, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1077, y = 1331, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1078, y = 1115, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1082, y = 1990, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1088, y = 2103, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1090, y = 1255, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1097, y = 2091, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1098, y = 1862, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1099, y = 1336, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1100, y = 1863, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1100, y = 2039, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1101, y = 1854, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1101, y = 2074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1102, y = 1876, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1103, y = 1850, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1103, y = 2084, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1108, y = 1992, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1110, y = 1964, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1111, y = 1873, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1114, y = 2072, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1115, y = 2056, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1121, y = 1282, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1121, y = 1312, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1123, y = 2093, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1125, y = 1834, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1127, y = 2077, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1131, y = 1962, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1143, y = 1877, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1153, y = 1896, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1154, y = 1888, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1161, y = 1903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1162, y = 1885, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1170, y = 1882, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1172, y = 1890, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1174, y = 1903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1178, y = 1813, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1179, y = 1976, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1181, y = 1897, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1183, y = 1809, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1185, y = 1963, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1186, y = 1819, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1186, y = 1822, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1186, y = 1832, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1192, y = 1989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1193, y = 1832, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1195, y = 1803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1196, y = 1811, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1196, y = 1833, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1199, y = 1980, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1200, y = 1825, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1217, y = 1814, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1226, y = 1876, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1228, y = 1906, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1230, y = 1844, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1241, y = 1799, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1243, y = 1807, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1246, y = 1899, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1253, y = 1807, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1254, y = 1814, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1258, y = 1818, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1259, y = 1841, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1260, y = 1809, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1260, y = 1869, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1264, y = 1812, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1266, y = 1903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1270, y = 1827, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1271, y = 1815, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1275, y = 1940, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1279, y = 1901, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1280, y = 1943, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1282, y = 1954, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1283, y = 1927, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1283, y = 1939, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1287, y = 1856, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1296, y = 1954, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1302, y = 1931, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1304, y = 1953, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1306, y = 1940, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1312, y = 1893, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1312, y = 1952, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1348, y = 1803, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1350, y = 1817, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1356, y = 1869, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1360, y = 1879, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1366, y = 1890, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1370, y = 1878, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1376, y = 1903, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1381, y = 1900, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1383, y = 1889, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1386, y = 1878, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 1390, y = 1884, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 797, y = 1168, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 819, y = 1158, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 833, y = 1221, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 840, y = 1210, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 844, y = 1219, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 844, y = 1226, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 854, y = 1220, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 865, y = 1191, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 871, y = 1217, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 884, y = 1201, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 889, y = 1022, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 889, y = 1303, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 893, y = 1187, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 893, y = 1198, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 895, y = 1317, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 911, y = 1054, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 915, y = 1061, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 915, y = 2156, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 917, y = 1046, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 918, y = 2144, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 922, y = 2173, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 922, y = 2180, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 923, y = 1038, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 927, y = 963, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 928, y = 1234, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 928, y = 2153, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 929, y = 1063, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 931, y = 2131, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 931, y = 2154, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 932, y = 1025, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 932, y = 967, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 936, y = 2146, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 937, y = 2195, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 939, y = 1103, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 939, y = 2172, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 941, y = 2183, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 943, y = 2153, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 945, y = 1268, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 945, y = 2171, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 946, y = 983, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 948, y = 1296, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 949, y = 1124, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 950, y = 2174, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 950, y = 2185, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 951, y = 1019, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 951, y = 1037, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 951, y = 949, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 954, y = 1326, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 955, y = 2165, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 958, y = 1030, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 960, y = 1209, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 962, y = 2155, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 966, y = 1283, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 972, y = 1868, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 974, y = 1269, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 984, y = 1887, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 984, y = 2156, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 987, y = 2036, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 995, y = 1939, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 996, y = 2139, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Orc Berserker", x = 997, y = 1097, z = 2, stationary = false, aggressive = true)
public final class r175OrcBerserker extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 8.wav";
  public static final String SOUND_DEATH = "Orc Dying.wav";
  public static final String SOUND_HIT = "Orc Hit.wav";

  public r175OrcBerserker(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Orc Berserker",
        "${monster.orc_berserker}",
        631,
        0,
        4,
        1311,
        30,
        69,
        30000L,
        "Orc#g",
        "OrcA#i",
        "OrcC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        57,
        176,
        java.util.List.of(
            new MonsterDef.LootDrop("Light healing potion", 0.02f),
            new MonsterDef.LootDrop("Orcish shield", 0.005f),
            new MonsterDef.LootDrop("Flask of crystal water", 1.0E-4f),
            new MonsterDef.LootDrop("Potion of fury", 0.01f),
            new MonsterDef.LootDrop("Ringmail helmet", 0.003f),
            new MonsterDef.LootDrop("Ringmail armor", 0.002f),
            new MonsterDef.LootDrop("Ringmail boots", 0.0034999999f),
            new MonsterDef.LootDrop("Ringmail leggings", 0.003f),
            new MonsterDef.LootDrop("Potion of Heroism", 0.01f)),
        false,
        0.0f,
        47,
        43,
        43,
        53,
        0,
        43,
        0,
        new int[] {105, 52, 78, 78, 78, 5000, 100, 100, 100, 100, 100, 100},
        32,
        138,
        0,
        1076887552,
        20008,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        15,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d40+29", 394, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
