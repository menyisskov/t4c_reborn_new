package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r201RoamingCadaver extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r201RoamingCadaver(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Roaming Cadaver",
        "${monster.roaming_cadaver}",
        996,
        0,
        5,
        3000,
        54,
        122,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        84,
        258,
        java.util.List.of(),
        false,
        0.0f,
        62,
        57,
        57,
        71,
        0,
        57,
        0,
        new int[] {71, 71, 95, 47, 5025, 47, 100, 100, 100, 100, 100, 100},
        46,
        190,
        0,
        1077280768,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d69+53", 574, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
