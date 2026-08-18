package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r255TROLLMADLIMBMANGLOR1 extends DataMonster {
  public static final String SOUND_ATTACK = "Troll Attack.wav";
  public static final String SOUND_DEATH = "Troll Dying.wav";
  public static final String SOUND_HIT = "Troll Hit.wav";

  public r255TROLLMADLIMBMANGLOR1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "TROLLMADLIMBMANGLOR1",
        "${monster.trollmadlimbmanglor1}",
        1,
        0,
        0,
        0,
        0,
        0,
        30000L,
        "Ooze#h",
        "OozeA#h",
        "OozeC#j",
        "Troll Attack.wav",
        "Troll Dying.wav",
        "Troll Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        new int[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
        0,
        0,
        0,
        0,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        false,
        java.util.List.of(),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
