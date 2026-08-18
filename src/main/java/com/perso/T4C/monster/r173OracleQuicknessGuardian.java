package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2732,
    y = 2284,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2736,
    y = 2296,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2744,
    y = 2280,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2748,
    y = 2268,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2748,
    y = 2292,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2764,
    y = 2252,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2768,
    y = 2272,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2780,
    y = 2260,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2784,
    y = 2232,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2792,
    y = 2247,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2800,
    y = 2216,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2804,
    y = 2236,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2812,
    y = 2212,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2816,
    y = 2200,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2824,
    y = 2216,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Quickness Guardian",
    x = 2828,
    y = 2204,
    z = 2,
    stationary = false,
    aggressive = true)
public final class r173OracleQuicknessGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public r173OracleQuicknessGuardian(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Oracle Quickness Guardian",
        "${monster.oracle_quickness_guardian}",
        4373,
        0,
        9,
        21714,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        215,
        660,
        java.util.List.of(),
        false,
        0.0f,
        135,
        122,
        122,
        159,
        0,
        122,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        120,
        490,
        0,
        1078853632,
        20036,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        1,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("", 0, 10, 10449, 2, 20),
            new MonsterDef.Attack("1d207+126", 1450, 100, 10292, 0, 1),
            new MonsterDef.Attack("", 0, 80, 10119, 2, 20),
            new MonsterDef.Attack("", 0, 10, 10382, 2, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
