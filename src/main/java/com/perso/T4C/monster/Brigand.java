package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "Brigand", x = 145, y = 1029, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 146, y = 1030, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 152, y = 877, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 153, y = 878, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 162, y = 1073, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 163, y = 1074, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1646, y = 1096, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1647, y = 1097, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1658, y = 1080, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1659, y = 1081, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 167, y = 951, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 168, y = 952, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1783, y = 1112, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1784, y = 1113, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1848, y = 1156, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1849, y = 1157, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1857, y = 1283, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1858, y = 1284, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1859, y = 1287, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1860, y = 1288, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1866, y = 1119, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1867, y = 1120, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1872, y = 1094, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1873, y = 1095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1877, y = 1307, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1878, y = 1308, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1898, y = 1231, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1899, y = 1232, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1910, y = 1221, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1911, y = 1222, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1934, y = 1029, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1935, y = 1030, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1958, y = 1052, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1959, y = 1053, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1963, y = 1084, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1964, y = 1085, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 197, y = 1048, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 198, y = 1049, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1980, y = 1106, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 1981, y = 1107, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 201, y = 839, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 202, y = 840, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2031, y = 1099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2032, y = 1100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2041, y = 1250, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2042, y = 1251, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2050, y = 1254, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2051, y = 1255, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2060, y = 1099, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2061, y = 1100, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2115, y = 1051, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2116, y = 1052, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 212, y = 945, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 213, y = 946, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2137, y = 1518, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2138, y = 1519, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2157, y = 1636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2158, y = 1637, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2158, y = 1373, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2159, y = 1374, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2158, y = 1622, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2159, y = 1623, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2159, y = 1372, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2160, y = 1373, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2160, y = 975, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2161, y = 976, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2167, y = 977, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2168, y = 978, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2173, y = 1635, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2174, y = 1636, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2177, y = 1379, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2178, y = 1380, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2177, y = 1640, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2178, y = 1641, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2201, y = 1534, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2202, y = 1535, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2217, y = 1499, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2218, y = 1500, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2232, y = 1492, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2233, y = 1493, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2232, y = 1493, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2233, y = 1494, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2253, y = 1167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2254, y = 1168, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2254, y = 1166, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2255, y = 1167, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 227, y = 1004, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 228, y = 1005, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 227, y = 889, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 228, y = 890, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2277, y = 1500, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2278, y = 1501, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2277, y = 928, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2278, y = 929, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2286, y = 945, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2287, y = 946, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2303, y = 1555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2304, y = 1556, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2315, y = 1304, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2316, y = 1305, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2316, y = 1545, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2317, y = 1546, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2323, y = 1294, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2324, y = 1295, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2335, y = 1243, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2336, y = 1244, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2345, y = 1206, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2346, y = 1207, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2348, y = 1096, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2349, y = 1097, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2355, y = 1429, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2356, y = 1430, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2358, y = 1086, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2359, y = 1087, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2372, y = 938, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2373, y = 939, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2373, y = 1436, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2374, y = 1437, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2374, y = 1066, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2375, y = 1067, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2383, y = 1260, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2384, y = 1261, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2391, y = 1499, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2392, y = 1500, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2410, y = 1084, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2411, y = 1085, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2410, y = 1094, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2411, y = 1095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2411, y = 1504, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2412, y = 1505, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2414, y = 1394, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2415, y = 1395, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2417, y = 988, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2418, y = 989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2429, y = 1400, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2430, y = 1401, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2434, y = 1186, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2435, y = 1187, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2458, y = 1070, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2459, y = 1071, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 247, y = 1156, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 248, y = 1157, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2470, y = 1074, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2471, y = 1075, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2471, y = 1241, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2472, y = 1242, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2478, y = 1277, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2479, y = 1278, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2482, y = 1396, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2483, y = 1397, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2482, y = 896, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2483, y = 897, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2483, y = 908, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2484, y = 909, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2493, y = 1040, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2494, y = 1041, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2500, y = 1053, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2501, y = 1054, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2501, y = 1301, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2502, y = 1302, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2503, y = 861, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2504, y = 862, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2504, y = 1241, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2505, y = 1242, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2507, y = 875, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2508, y = 876, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 251, y = 1113, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 252, y = 1114, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2511, y = 1057, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2512, y = 1058, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2523, y = 1277, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2524, y = 1278, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 262, y = 1042, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 263, y = 1043, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2638, y = 1095, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2639, y = 1096, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2639, y = 1093, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2640, y = 1094, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2640, y = 1093, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2641, y = 1094, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2644, y = 861, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2645, y = 862, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 265, y = 999, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 266, y = 1000, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2651, y = 897, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2652, y = 898, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2658, y = 989, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2659, y = 990, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2668, y = 830, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2669, y = 831, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 267, y = 945, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 268, y = 946, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2679, y = 1005, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2680, y = 1006, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2699, y = 1017, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2700, y = 1018, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2711, y = 846, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2712, y = 847, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2743, y = 863, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 2744, y = 864, z = 0, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 282, y = 1022, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 283, y = 1023, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 285, y = 1181, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 286, y = 1182, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 289, y = 939, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 290, y = 940, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 296, y = 915, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 297, y = 916, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 296, y = 918, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 297, y = 919, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 299, y = 914, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 300, y = 915, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 299, y = 921, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 300, y = 922, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 314, y = 900, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 315, y = 901, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 325, y = 984, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 326, y = 985, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 333, y = 965, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 334, y = 966, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 339, y = 1054, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 340, y = 1055, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 354, y = 1111, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 355, y = 1112, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 363, y = 924, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 364, y = 925, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 368, y = 1065, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 369, y = 1066, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 377, y = 939, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 378, y = 940, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 389, y = 845, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 390, y = 846, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 390, y = 1036, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 391, y = 1037, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 407, y = 831, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 408, y = 832, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 418, y = 1004, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 419, y = 1005, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 428, y = 900, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 429, y = 901, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 454, y = 816, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 455, y = 817, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 458, y = 956, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 459, y = 957, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 459, y = 818, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 460, y = 819, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 460, y = 757, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 461, y = 758, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 481, y = 830, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 482, y = 831, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 482, y = 1025, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 483, y = 1026, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 485, y = 855, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 486, y = 856, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 491, y = 917, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 492, y = 918, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 501, y = 980, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 502, y = 981, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 504, y = 978, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 505, y = 979, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 509, y = 894, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 510, y = 895, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 509, y = 902, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 510, y = 903, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 512, y = 882, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 513, y = 883, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 527, y = 919, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 528, y = 920, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 530, y = 797, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 531, y = 798, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 546, y = 668, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 547, y = 669, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 553, y = 800, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 554, y = 801, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 555, y = 799, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 556, y = 800, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 556, y = 802, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 557, y = 803, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 556, y = 855, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 557, y = 856, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 558, y = 856, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 559, y = 857, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 560, y = 853, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 561, y = 854, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 560, y = 943, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 561, y = 944, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 574, y = 765, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 575, y = 766, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 575, y = 767, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 576, y = 768, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 577, y = 764, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 578, y = 765, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 578, y = 766, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 579, y = 767, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 579, y = 713, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 580, y = 714, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 596, y = 839, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 597, y = 840, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 596, y = 842, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 597, y = 843, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 598, y = 840, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 599, y = 841, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 600, y = 769, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 601, y = 770, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 601, y = 793, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 602, y = 794, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 606, y = 907, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 607, y = 908, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 607, y = 907, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 608, y = 908, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 607, y = 908, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 608, y = 909, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 608, y = 908, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 609, y = 909, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 608, y = 909, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 609, y = 910, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 612, y = 868, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 613, y = 869, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 613, y = 868, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 614, y = 869, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 623, y = 842, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 624, y = 843, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 627, y = 847, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 628, y = 848, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 627, y = 848, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 628, y = 849, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 630, y = 849, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 631, y = 850, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 634, y = 785, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 635, y = 786, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 637, y = 921, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 638, y = 922, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 639, y = 924, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 640, y = 925, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 641, y = 922, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 642, y = 923, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 643, y = 923, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 644, y = 924, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 648, y = 785, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 649, y = 786, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 648, y = 787, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 649, y = 788, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 648, y = 788, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 649, y = 789, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 649, y = 873, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 650, y = 874, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 650, y = 863, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 651, y = 864, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 650, y = 871, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 651, y = 872, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 651, y = 873, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 652, y = 874, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 653, y = 789, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 654, y = 790, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 659, y = 810, z = 2, stationary = false, aggressive = true)
@Spawn(type = "Brigand", x = 660, y = 811, z = 2, stationary = false, aggressive = true)
public final class Brigand extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 1.wav";
  public static final String SOUND_DEATH = "Male Dying 1.wav";
  public static final String SOUND_HIT = "Male Hit 1.wav";

  public static final String CANONICAL_NAME = "Brigand";

  public Brigand(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Brigand",
        "${monster.brigand}",
        116,
        0,
        1,
        112,
        6,
        15,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC!a",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        12,
        38,
        java.util.List.of(
            new MonsterDef.LootDrop("Leather gloves", 0.01f),
            new MonsterDef.LootDrop("Leather pants", 0.01f),
            new MonsterDef.LootDrop("Leather Helmet", 0.01f),
            new MonsterDef.LootDrop("Light healing potion", 0.15f),
            new MonsterDef.LootDrop("Iron key", 0.01f),
            new MonsterDef.LootDrop("diamond", 0.15f),
            new MonsterDef.LootDrop("secret_document", 0.1f),
            new MonsterDef.LootDrop("finely_crafted_drum", 0.15f)),
        false,
        0.0f,
        21,
        21,
        21,
        23,
        0,
        21,
        0,
        new int[] {90, 90, 90, 90, 60, 5000, 100, 100, 100, 100, 100, 100},
        7,
        43,
        0,
        1073741824,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        40,
        11,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10+5", 94, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
