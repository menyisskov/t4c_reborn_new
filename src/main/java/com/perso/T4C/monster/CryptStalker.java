package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class CryptStalker extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public static final String CANONICAL_NAME = "Crypt Stalker";

  public CryptStalker(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Crypt Stalker",
        "${monster.crypt_stalker}",
        936,
        0,
        5,
        2440,
        46,
        105,
        30000L,
        "Spider#f",
        "SpiderA#f",
        "SpiderC#m",
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        77,
        236,
        java.util.List.of(),
        false,
        0.0f,
        58,
        53,
        53,
        66,
        0,
        53,
        0,
        new int[] {98, 49, 73, 73, 73, 0, 100, 100, 100, 100, 100, 100},
        43,
        182,
        0,
        1077215232,
        20007,
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
        java.util.List.of(new MonsterDef.Attack("1d60+45", 526, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
