package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CentaurArcher extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshm 7.wav";
  public static final String SOUND_DEATH = "Taunting Hit.wav";
  public static final String SOUND_HIT = "Centaur Hit.wav";

  public static final String CANONICAL_NAME = "Centaur Archer";

  public CentaurArcher(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Centaur Archer",
        "${monster.centaur_archer}",
        1000,
        0,
        0,
        0,
        1,
        100,
        30000L,
        "64kCentaurArcher#i",
        "64kCentaurArcherA",
        "64kCentaurArcherC",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        0,
        0,
        java.util.List.of(),
        false,
        0.0f,
        100,
        100,
        100,
        100,
        0,
        100,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        100,
        100,
        0,
        1079574528,
        20052,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        -100,
        42,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d100", 100, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
