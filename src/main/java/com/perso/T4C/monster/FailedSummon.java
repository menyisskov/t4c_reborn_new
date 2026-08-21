package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class FailedSummon extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Failed Summon";

  public FailedSummon(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Failed Summon",
        "${monster.failed_summon}",
        710,
        0,
        4,
        1566,
        34,
        78,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        62,
        192,
        java.util.List.of(),
        false,
        0.0f,
        50,
        46,
        46,
        57,
        0,
        46,
        0,
        new int[] {51, 103, 103, 51, 77, 5000, 100, 100, 100, 100, 100, 100},
        35,
        60,
        0,
        1078034432,
        20005,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        23,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d45+33", 430, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
