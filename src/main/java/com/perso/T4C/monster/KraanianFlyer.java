package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class KraanianFlyer extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Flyer";

  public KraanianFlyer(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Flyer",
        "${monster.kraanian_flyer}",
        293,
        0,
        2,
        413,
        14,
        33,
        30000L,
        "",
        null,
        null,
        "Kraanian Attack.wav",
        "Kraanian Dying.wav",
        "Kraanian Hit.wav",
        30,
        93,
        java.util.List.of(),
        false,
        0.0f,
        32,
        30,
        30,
        35,
        0,
        30,
        0,
        new int[] {57, 114, 86, 86, 86, 5000, 100, 100, 100, 100, 100, 100},
        17,
        118,
        0,
        0,
        20034,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        18,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d20+13", 214, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 1, 20)),
        false,
        0,
        java.util.List.of("KraanianFlying"),
        java.util.Map.of());
  }
}
