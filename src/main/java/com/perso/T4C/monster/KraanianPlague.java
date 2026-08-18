package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class KraanianPlague extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Plague";

  public KraanianPlague(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Plague",
        "${monster.kraanian_plague}",
        1684,
        0,
        6,
        5780,
        78,
        176,
        30000L,
        "",
        null,
        null,
        "Kraanian Attack.wav",
        "Kraanian Dying.wav",
        "Kraanian Hit.wav",
        116,
        357,
        java.util.List.of(),
        false,
        0.0f,
        80,
        73,
        73,
        93,
        0,
        73,
        0,
        new int[] {42, 84, 63, 63, 63, 5000, 100, 100, 100, 100, 100, 100},
        65,
        270,
        0,
        1077936128,
        20034,
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
        java.util.List.of(new MonsterDef.Attack("1d99+77", 790, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
