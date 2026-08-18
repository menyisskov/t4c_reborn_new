package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class KraanianWorker extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Worker";

  public KraanianWorker(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Worker",
        "${monster.kraanian_worker}",
        235,
        0,
        2,
        301,
        12,
        27,
        30000L,
        "Kraanian#h",
        "KraanianA#h",
        "KraanianC#l",
        "Kraanian Attack.wav",
        "Kraanian Dying.wav",
        "Kraanian Hit.wav",
        25,
        77,
        java.util.List.of(),
        false,
        0.0f,
        28,
        32,
        27,
        29,
        0,
        27,
        0,
        new int[] {116, 58, 87, 87, 87, 5000, 100, 100, 100, 100, 100, 100},
        14,
        66,
        0,
        1075576832,
        20025,
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
            new MonsterDef.Attack("1d16+11", 178, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 5, 20)),
        false,
        0,
        java.util.List.of("Kraanian"),
        java.util.Map.of());
  }
}
