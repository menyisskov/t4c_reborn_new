package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r173OracleQuicknessGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

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
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
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
