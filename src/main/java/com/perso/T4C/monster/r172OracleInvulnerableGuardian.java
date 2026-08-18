package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r172OracleInvulnerableGuardian extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r172OracleInvulnerableGuardian(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Oracle Invulnerable Guardian",
        "${monster.oracle_invulnerable_guardian}",
        498800,
        0,
        0,
        0,
        125,
        348,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        145,
        131,
        131,
        171,
        0,
        131,
        0,
        new int[] {47, 63, 47, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        130,
        780,
        0,
        1090021872,
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
        java.util.List.of(new MonsterDef.Attack("1d224+124", 1820, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
