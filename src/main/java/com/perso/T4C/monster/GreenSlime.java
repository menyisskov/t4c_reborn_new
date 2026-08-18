package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class GreenSlime extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Green Slime";

  public GreenSlime(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Green Slime",
        "${monster.green_slime}",
        41,
        0,
        1,
        34,
        3,
        7,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        "Ooze Attack.wav",
        "Ooze Dying.wav",
        "Ooze Hit.wav",
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
        new int[] {61, 123, 123, 61, 93, 5000, 100, 100, 100, 100, 100, 100},
        2,
        8,
        0,
        1074266112,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        60,
        16,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d5+2", 34, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
