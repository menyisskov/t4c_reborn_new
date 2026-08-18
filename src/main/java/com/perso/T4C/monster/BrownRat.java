package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class BrownRat extends DataMonster {
  public static final String SOUND_ATTACK = "Rat Attack.wav";
  public static final String SOUND_DEATH = "Rat Dying.wav";
  public static final String SOUND_HIT = "Rat Hit.wav";

  public static final String CANONICAL_NAME = "Brown Rat";

  public BrownRat(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Brown Rat",
        "${monster.brown_rat}",
        27,
        0,
        1,
        21,
        2,
        5,
        30000L,
        "Rat#f",
        "RatA#i",
        "RatC#j",
        "Rat Attack.wav",
        "Rat Dying.wav",
        "Rat Hit.wav",
        1,
        5,
        java.util.List.of(new MonsterDef.LootDrop("Torch", 0.02f)),
        false,
        0.0f,
        16,
        15,
        15,
        16,
        0,
        15,
        0,
        new int[] {124, 62, 93, 93, 93, 5000, 100, 100, 100, 100, 100, 100},
        1,
        14,
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
        java.util.List.of(new MonsterDef.Attack("1d4+1", 22, 100, 0, 0, 0)),
        true,
        10,
        java.util.List.of("Rat"),
        java.util.Map.of());
  }
}
