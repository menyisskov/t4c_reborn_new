package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Apparition extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Apparition";

  public Apparition(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Apparition",
        "${monster.apparition}",
        1321,
        0,
        6,
        4062,
        63,
        143,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        98,
        302,
        java.util.List.of(),
        false,
        0.0f,
        70,
        64,
        64,
        81,
        0,
        64,
        0,
        new int[] {68, 68, 68, 68, 90, 5000, 100, 100, 100, 100, 100, 100},
        55,
        230,
        0,
        1077608448,
        21042,
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
        java.util.List.of(new MonsterDef.Attack("1d81+62", 670, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
