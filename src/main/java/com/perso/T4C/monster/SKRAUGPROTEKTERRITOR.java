package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.monster.core.MonsterGoldCurve;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1003,
    y = 464,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1004,
    y = 465,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1003,
    y = 570,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1004,
    y = 571,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1022,
    y = 454,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1023,
    y = 455,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1023,
    y = 640,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1024,
    y = 641,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1025,
    y = 501,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1026,
    y = 502,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1026,
    y = 602,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1027,
    y = 603,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1109,
    y = 445,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1110,
    y = 446,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1216,
    y = 478,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1217,
    y = 479,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1306,
    y = 512,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1307,
    y = 513,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1947,
    y = 1386,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1948,
    y = 1387,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1964,
    y = 1371,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1965,
    y = 1372,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1997,
    y = 1292,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1998,
    y = 1293,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1997,
    y = 1355,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 1998,
    y = 1356,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2014,
    y = 1271,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2015,
    y = 1272,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2021,
    y = 1364,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2022,
    y = 1365,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2039,
    y = 1389,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2040,
    y = 1390,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2041,
    y = 1372,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2042,
    y = 1373,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2085,
    y = 1261,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2086,
    y = 1262,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2106,
    y = 1518,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2107,
    y = 1519,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2144,
    y = 1487,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2145,
    y = 1488,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2148,
    y = 1534,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2149,
    y = 1535,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2167,
    y = 1502,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2168,
    y = 1503,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2173,
    y = 1423,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2174,
    y = 1424,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2373,
    y = 1280,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2374,
    y = 1281,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2388,
    y = 1291,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2389,
    y = 1292,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2432,
    y = 1505,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2433,
    y = 1506,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2454,
    y = 1504,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2455,
    y = 1505,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2511,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2511,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2511,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2512,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2513,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 600,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2514,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 609,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 600,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2515,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 609,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 600,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2516,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 609,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2517,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 601,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 602,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2518,
    y = 607,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 608,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 603,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2520,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 604,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2520,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2519,
    y = 605,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2520,
    y = 606,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2554,
    y = 1397,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2555,
    y = 1398,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2562,
    y = 585,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2563,
    y = 586,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2563,
    y = 1356,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2564,
    y = 1357,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2571,
    y = 1447,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2572,
    y = 1448,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2581,
    y = 655,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2582,
    y = 656,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2598,
    y = 1463,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2599,
    y = 1464,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2687,
    y = 566,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2688,
    y = 567,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2688,
    y = 630,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2689,
    y = 631,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2706,
    y = 578,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2707,
    y = 579,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2726,
    y = 659,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2727,
    y = 660,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2766,
    y = 652,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2767,
    y = 653,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2783,
    y = 368,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2784,
    y = 369,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2809,
    y = 366,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2810,
    y = 367,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2843,
    y = 627,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2844,
    y = 628,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2845,
    y = 420,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2846,
    y = 421,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2854,
    y = 574,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2855,
    y = 575,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2875,
    y = 489,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2876,
    y = 490,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2880,
    y = 452,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2881,
    y = 453,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2895,
    y = 636,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 2896,
    y = 637,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 756,
    y = 546,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 757,
    y = 547,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 764,
    y = 501,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 765,
    y = 502,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 771,
    y = 512,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 772,
    y = 513,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 780,
    y = 453,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 781,
    y = 454,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 784,
    y = 533,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 785,
    y = 534,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 803,
    y = 524,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 804,
    y = 525,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 817,
    y = 558,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 818,
    y = 559,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 827,
    y = 582,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 828,
    y = 583,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 828,
    y = 430,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 829,
    y = 431,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 843,
    y = 454,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 844,
    y = 455,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 856,
    y = 527,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 857,
    y = 528,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 856,
    y = 539,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 857,
    y = 540,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 861,
    y = 500,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 862,
    y = 501,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 870,
    y = 573,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 871,
    y = 574,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 871,
    y = 475,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 872,
    y = 476,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 871,
    y = 487,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 872,
    y = 488,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 873,
    y = 446,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 874,
    y = 447,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 894,
    y = 520,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 895,
    y = 521,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 895,
    y = 553,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 896,
    y = 554,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 901,
    y = 421,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 902,
    y = 422,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 926,
    y = 484,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 927,
    y = 485,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 932,
    y = 587,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 933,
    y = 588,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 960,
    y = 515,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 961,
    y = 516,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 961,
    y = 466,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 962,
    y = 467,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 962,
    y = 447,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 963,
    y = 448,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 987,
    y = 498,
    z = 0,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "SKRAUGPROTEKTERRITOR",
    x = 988,
    y = 499,
    z = 0,
    stationary = false,
    aggressive = true)
public final class SKRAUGPROTEKTERRITOR extends DataMonster {
  public static final String SOUND_ATTACK = "Skraug Attack.wav";
  public static final String SOUND_DEATH = "Skraug Die.wav";
  public static final String SOUND_HIT = "Taunting Hit.wav";

  public SKRAUGPROTEKTERRITOR(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "SKRAUGPROTEKTERRITOR",
        "${monster.skraugprotekterritor}",
        1426,
        0,
        31,
        22704,
        67,
        153,
        30000L,
        "64kSkavenPeon#i",
        "64kSkavenPeonA#j",
        "64kSkavenPeonC!t",
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
        73,
        67,
        67,
        84,
        67,
        67,
        26,
        new int[] {66, 66, 66, 66, 66, 5000, 100, 100, 100, 100, 100, 100},
        150,
        242,
        0,
        1105723392,
        20047,
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
        java.util.List.of(new MonsterDef.Attack("1d87+66", 706, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
