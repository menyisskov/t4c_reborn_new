package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1014, y = 528, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1015, y = 529, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1014, y = 552, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1015, y = 553, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1021, y = 626, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1022, y = 627, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1023, y = 651, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1024, y = 652, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1113, y = 423, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1114, y = 424, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1226, y = 582, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1227, y = 583, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1236, y = 469, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1237, y = 470, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1968, y = 1388, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 1969, y = 1389, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2066, y = 1418, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2067, y = 1419, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2069, y = 1397, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2070, y = 1398, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2080, y = 1332, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2081, y = 1333, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2081, y = 1412, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2082, y = 1413, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2102, y = 1308, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2103, y = 1309, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2141, y = 1292, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2142, y = 1293, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2149, y = 1565, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2150, y = 1566, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2172, y = 1546, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2173, y = 1547, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2186, y = 1564, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2187, y = 1565, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2186, y = 1581, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2187, y = 1582, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2200, y = 1416, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2201, y = 1417, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2236, y = 1420, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2237, y = 1421, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2243, y = 1568, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2244, y = 1569, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2246, y = 1491, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2247, y = 1492, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2317, y = 1348, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2318, y = 1349, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2411, y = 1385, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2412, y = 1386, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2421, y = 1532, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2422, y = 1533, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2426, y = 1382, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2427, y = 1383, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2440, y = 1508, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2441, y = 1509, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2470, y = 1527, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2471, y = 1528, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2473, y = 1506, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2474, y = 1507, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2510, y = 1399, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2511, y = 1400, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2511, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2511, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2511, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2512, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2513, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2514, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 620, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2515, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 620, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2516, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 620, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2517, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 613, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2518, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 619, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 614, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2520, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 615, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2520, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2519, y = 616, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2520, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2582, y = 1469, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2583, y = 1470, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2590, y = 489, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2591, y = 490, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2614, y = 611, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2615, y = 612, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2618, y = 520, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2619, y = 521, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2630, y = 462, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2631, y = 463, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2649, y = 422, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2650, y = 423, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2651, y = 538, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2652, y = 539, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2663, y = 468, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2664, y = 469, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2708, y = 444, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2709, y = 445, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2710, y = 617, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2711, y = 618, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2716, y = 371, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2717, y = 372, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2718, y = 537, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2719, y = 538, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2735, y = 588, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2736, y = 589, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2754, y = 535, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2755, y = 536, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2766, y = 470, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2767, y = 471, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2792, y = 484, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2793, y = 485, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2793, y = 418, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2794, y = 419, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2805, y = 603, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2806, y = 604, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2846, y = 499, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2847, y = 500, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2847, y = 378, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2848, y = 379, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2860, y = 396, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2861, y = 397, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2904, y = 530, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 2905, y = 531, z = 2, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 751, y = 525, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 752, y = 526, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 775, y = 469, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 776, y = 470, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 779, y = 520, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 780, y = 521, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 784, y = 554, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 785, y = 555, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 818, y = 439, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 819, y = 440, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 818, y = 580, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 819, y = 581, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 836, y = 486, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 837, y = 487, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 845, y = 539, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 846, y = 540, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 875, y = 521, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 876, y = 522, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 884, y = 489, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 885, y = 490, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 891, y = 456, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 892, y = 457, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 915, y = 482, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 916, y = 483, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 923, y = 515, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 924, y = 516, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 942, y = 564, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 943, y = 565, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 950, y = 597, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 951, y = 598, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 952, y = 488, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 953, y = 489, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 981, y = 612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 982, y = 613, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 982, y = 612, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 983, y = 613, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 989, y = 481, z = 0, stationary = false, aggressive = true)
@Spawn(type = "SKRAUGBLUDFIGHTOR", x = 990, y = 482, z = 0, stationary = false, aggressive = true)
public final class SKRAUGBLUDFIGHTOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGBLUDFIGHTOR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGBLUDFIGHTOR",
        "${monster.skraugbludfightor}",
        1801,
        0,
        35,
        31845,
        82,
        187,
        30000L,
        "64kSkavenSkavenger#i",
        "64kSkavenSkavengerA#i",
        "64kSkavenSkavengerC!s",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        MonsterGoldCurve.goldMin(150),
        MonsterGoldCurve.goldMax(150),
        java.util.List.of(
            new MonsterDef.LootDrop("blade_of_heroism", 0.3f),
            new MonsterDef.LootDrop("moon_tug_scalp", 0.3f),
            new MonsterDef.LootDrop("raw_crystal", 0.3f)),
        false,
        0.0f,
        83,
        76,
        76,
        96,
        76,
        76,
        28,
        new int[] {62, 62, 62, 62, 62, 5000, 100, 100, 100, 100, 100, 100},
        150,
        282,
        0,
        1107820544,
        20049,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d106+81", 826, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
