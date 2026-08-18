package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2808,
    y = 2256,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2816,
    y = 2240,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2820,
    y = 2260,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2828,
    y = 2252,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2836,
    y = 2236,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2836,
    y = 2244,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2848,
    y = 2224,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 1",
    x = 2848,
    y = 2240,
    z = 2,
    stationary = false,
    aggressive = true)
public final class r170OracleGateGuardian1 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r170OracleGateGuardian1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Oracle Gate Guardian 1",
        "${monster.oracle_gate_guardian_1}",
        4677,
        0,
        10,
        23665,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        224,
        687,
        java.util.List.of(),
        false,
        0.0f,
        140,
        127,
        127,
        165,
        0,
        127,
        0,
        new int[] {31, 63, 31, 63, 47, 5000, 100, 100, 100, 100, 100, 100},
        125,
        510,
        0,
        1078919168,
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
            new MonsterDef.Attack("", 0, 80, 10096, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10424, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10426, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10428, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10430, 1, 20),
            new MonsterDef.Attack("1d215+128", 1510, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10382, 1, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
