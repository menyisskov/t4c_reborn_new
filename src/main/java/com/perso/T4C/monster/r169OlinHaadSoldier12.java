package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r169OlinHaadSoldier12 extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r169OlinHaadSoldier12(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Soldier 12",
        "${monster.olin_haad_soldier_12}",
        199,
        0,
        2,
        235,
        10,
        23,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        21,
        66,
        java.util.List.of(),
        false,
        0.0f,
        27,
        25,
        25,
        29,
        0,
        25,
        0,
        new int[] {88, 88, 88, 88, 58, 5000, 100, 100, 100, 100, 100, 100},
        12,
        58,
        0,
        1075314688,
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
        java.util.List.of(new MonsterDef.Attack("1d14+9", 154, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
