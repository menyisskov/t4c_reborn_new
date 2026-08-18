package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r163OlinHaadGuard3 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r163OlinHaadGuard3(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Guard 3",
        "${monster.olin_haad_guard_3}",
        55,
        0,
        1,
        46,
        4,
        8,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        5,
        16,
        java.util.List.of(),
        false,
        0.0f,
        18,
        17,
        17,
        18,
        0,
        17,
        0,
        new int[] {92, 92, 92, 92, 61, 5000, 100, 100, 100, 100, 100, 100},
        3,
        22,
        0,
        1072693248,
        20006,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+3", 46, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
