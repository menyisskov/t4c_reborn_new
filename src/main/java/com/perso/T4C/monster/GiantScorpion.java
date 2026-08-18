package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GiantScorpion extends DataMonster {
  public static final String SOUND_ATTACK = "Elemear Attack.wav";
  public static final String SOUND_DEATH = "Scorpion Dying.wav";
  public static final String SOUND_HIT = "Scorpion Hit.wav";

  public static final String CANONICAL_NAME = "Giant Scorpion";

  public GiantScorpion(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Giant Scorpion",
        "${monster.giant_scorpion}",
        510,
        0,
        3,
        933,
        24,
        55,
        30000L,
        "Scorpion#h",
        "ScorpionA#g",
        "ScorpionC#l",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        48,
        148,
        java.util.List.of(),
        false,
        0.0f,
        42,
        39,
        39,
        47,
        0,
        39,
        0,
        new int[] {81, 81, 54, 108, 81, 5000, 100, 100, 100, 100, 100, 100},
        27,
        83,
        0,
        1077149696,
        20024,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        24,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d32+23", 334, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
