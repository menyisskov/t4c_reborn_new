package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class KraanianMilipede extends DataMonster {
  public static final String SOUND_ATTACK = "Kraanian Attack.wav";
  public static final String SOUND_DEATH = "Kraanian Dying.wav";
  public static final String SOUND_HIT = "Kraanian Hit.wav";

  public static final String CANONICAL_NAME = "Kraanian Milipede";

  public KraanianMilipede(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Kraanian Milipede",
        "${monster.kraanian_milipede}",
        313,
        0,
        2,
        453,
        15,
        35,
        30000L,
        "",
        null,
        null,
        "Kraanian Attack.wav",
        "Kraanian Dying.wav",
        "Kraanian Hit.wav",
        32,
        99,
        java.util.List.of(new MonsterDef.LootDrop("Kraanian egg", 0.02f)),
        false,
        0.0f,
        33,
        31,
        31,
        36,
        0,
        31,
        0,
        new int[] {113, 56, 85, 85, 85, 5000, 100, 100, 100, 100, 100, 100},
        18,
        82,
        0,
        1075970048,
        20035,
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
        java.util.List.of(new MonsterDef.Attack("1d21+14", 226, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
