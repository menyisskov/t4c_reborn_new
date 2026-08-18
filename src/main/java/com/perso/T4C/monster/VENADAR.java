package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class VENADAR extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public VENADAR(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "VENADAR",
        "${monster.venadar}",
        1879,
        0,
        0,
        0,
        85,
        194,
        30000L,
        "Taunting#h",
        "TauntingA#h",
        "TauntingC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        85,
        77,
        77,
        99,
        77,
        77,
        29,
        new int[] {40, 81, 40, 81, 61, 5000, 100, 100, 100, 100, 100, 100},
        100,
        290,
        0,
        1108082688,
        20038,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        0,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d110+84", 850, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
