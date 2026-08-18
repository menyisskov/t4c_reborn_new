package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r165OlinHaadGuard5 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r165OlinHaadGuard5(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Guard 5",
        "${monster.olin_haad_guard_5}",
        84,
        0,
        1,
        77,
        5,
        12,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        8,
        27,
        java.util.List.of(),
        false,
        0.0f,
        20,
        19,
        19,
        21,
        0,
        19,
        0,
        new int[] {91, 91, 91, 91, 60, 5000, 100, 100, 100, 100, 100, 100},
        5,
        30,
        0,
        1073741824,
        20006,
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
        java.util.List.of(new MonsterDef.Attack("1d8+4", 70, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
