package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r185PlagueRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public r185PlagueRat(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Plague Rat",
        "${monster.plague_rat}",
        235,
        0,
        2,
        303,
        12,
        27,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        25,
        77,
        java.util.List.of(),
        false,
        0.0f,
        29,
        27,
        27,
        31,
        0,
        27,
        0,
        new int[] {116, 58, 87, 87, 87, 5000, 100, 100, 100, 100, 100, 100},
        14,
        76,
        0,
        0,
        20003,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        16,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d16+11", 206, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
