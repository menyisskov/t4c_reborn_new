package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

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
