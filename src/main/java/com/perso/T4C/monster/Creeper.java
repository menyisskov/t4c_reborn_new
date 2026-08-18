package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Creeper extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Creeper";

  public Creeper(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Creeper",
        "${monster.creeper}",
        1219,
        0,
        5,
        3615,
        59,
        133,
        30000L,
        "Atrocity#h",
        "AtrocityA#h",
        "AtrocityC#k",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        93,
        286,
        java.util.List.of(),
        false,
        0.0f,
        67,
        61,
        61,
        77,
        0,
        61,
        0,
        new int[] {69, 69, 46, 92, 69, 5000, 100, 100, 100, 100, 100, 100},
        52,
        218,
        0,
        1077542912,
        20026,
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
        java.util.List.of(new MonsterDef.Attack("1d75+58", 634, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
