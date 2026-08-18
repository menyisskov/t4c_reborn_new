package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r253TROLLCLANGBANGAH1 extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public r253TROLLCLANGBANGAH1(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "TROLLCLANGBANGAH1",
        "${monster.trollclangbangah1}",
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
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
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
