package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class LargeRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Large Rat";

  public LargeRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Large Rat",
        "${monster.large_rat}",
        41,
        0,
        1,
        34,
        3,
        7,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC!j",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        3,
        11,
        java.util.List.of(),
        false,
        0.0f,
        15,
        10,
        25,
        17,
        0,
        16,
        0,
        new int[] {123, 61, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        2,
        10,
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
        java.util.List.of(new MonsterDef.Attack("1d5+2", 39, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
