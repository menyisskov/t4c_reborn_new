package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class PIG extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Pig Dying.wav";
  public static final String SOUND_HIT = "Pig Hit.wav";

  public PIG(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "PIG",
        "${monster.pig}",
        300,
        0,
        0,
        0,
        1,
        4,
        30000L,
        "Warrio#l",
        "WarrioA#l",
        "WarrioC",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        20,
        20,
        25,
        55,
        55,
        55,
        55,
        new int[] {75, 75, 75, 75, 75, 5000, 100, 100, 100, 100, 100, 100},
        10,
        30,
        0,
        1073741824,
        20031,
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
        java.util.List.of(new MonsterDef.Attack("1d4", 50, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
