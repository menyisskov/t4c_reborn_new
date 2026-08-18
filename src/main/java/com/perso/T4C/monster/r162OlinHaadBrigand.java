package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r162OlinHaadBrigand extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r162OlinHaadBrigand(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Brigand",
        "${monster.olin_haad_brigand}",
        353,
        0,
        3,
        543,
        17,
        39,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        35,
        110,
        java.util.List.of(),
        false,
        0.0f,
        35,
        32,
        32,
        39,
        0,
        32,
        0,
        new int[] {84, 84, 84, 84, 56, 5000, 100, 100, 100, 100, 100, 100},
        20,
        90,
        0,
        1076101120,
        10011,
        40023,
        40022,
        0,
        0,
        40021,
        40062,
        0,
        0,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d23+16", 250, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
