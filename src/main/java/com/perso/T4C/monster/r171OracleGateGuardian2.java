package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;
import com.perso.T4C.spawn.Spawn;

@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2756,
    y = 2316,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2768,
    y = 2304,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2772,
    y = 2284,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2772,
    y = 2316,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2776,
    y = 2304,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2784,
    y = 2296,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2788,
    y = 2276,
    z = 2,
    stationary = false,
    aggressive = true)
@Spawn(
    type = "Oracle Gate Guardian 2",
    x = 2792,
    y = 2288,
    z = 2,
    stationary = false,
    aggressive = true)
public final class r171OracleGateGuardian2 extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public r171OracleGateGuardian2(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Oracle Gate Guardian 2",
        "${monster.oracle_gate_guardian_2}",
        4677,
        0,
        10,
        23665,
        0,
        0,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC!p",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
            new MonsterDef.Attack("", 0, 70, 10096, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10425, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10427, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10429, 1, 20),
            new MonsterDef.Attack("", 0, 5, 10431, 1, 20),
            new MonsterDef.Attack("1d215+128", 1510, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 10, 10382, 1, 20)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
