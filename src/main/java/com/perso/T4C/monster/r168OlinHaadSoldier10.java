package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r168OlinHaadSoldier10 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r168OlinHaadSoldier10(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Soldier 10",
        "${monster.olin_haad_soldier_10}",
        164,
        0,
        2,
        184,
        9,
        20,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        17,
        55,
        java.util.List.of(),
        false,
        0.0f,
        25,
        23,
        23,
        27,
        0,
        23,
        0,
        new int[] {89, 89, 89, 89, 59, 5000, 100, 100, 100, 100, 100, 100},
        10,
        50,
        0,
        1075052544,
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
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d12+8", 130, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
