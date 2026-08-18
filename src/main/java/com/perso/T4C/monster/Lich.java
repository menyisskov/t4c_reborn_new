package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class Lich extends DataMonster {
  public static final String SOUND_ATTACK = "Whooshh 3.wav";
  public static final String SOUND_DEATH = "Vampire Dying.wav";
  public static final String SOUND_HIT = "Vampire Hit.wav";

  public static final String CANONICAL_NAME = "Lich";

  public Lich(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Lich",
        "${monster.lich}",
        10000,
        0,
        0,
        0,
        1,
        10,
        30000L,
        "64kLich#l",
        "64kLichA#j",
        "64kLichC#w",
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
        20058,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d10", 50, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
