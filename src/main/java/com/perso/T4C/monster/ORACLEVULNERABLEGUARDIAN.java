package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class ORACLEVULNERABLEGUARDIAN extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public ORACLEVULNERABLEGUARDIAN(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "ORACLEVULNERABLEGUARDIAN",
        "${monster.oraclevulnerableguardian}",
        4988,
        0,
        51,
        128457,
        125,
        348,
        30000L,
        "Agmorkian#h",
        "AgmorkianA#h",
        "AgmorkianC#p",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        233,
        715,
        java.util.List.of(),
        false,
        0.0f,
        145,
        131,
        131,
        171,
        131,
        131,
        41,
        new int[] {31, 63, 31, 63, 47, 5000, 100, 100, 100, 100, 100, 100},
        130,
        780,
        0,
        1115815936,
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
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d224+124", 1820, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
