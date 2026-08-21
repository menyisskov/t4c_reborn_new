package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class AcidSlime extends DataMonster {
  public static final String SOUND_ATTACK = "Ooze Attack.wav";
  public static final String SOUND_DEATH = "Ooze Dying.wav";
  public static final String SOUND_HIT = "Ooze Hit.wav";

  public static final String CANONICAL_NAME = "Acid Slime";

  public AcidSlime(MonsterDef definition, float x, float y) throws GameException {

    super(definition, x, y);
  }

  @Override
  public String getCanonicalName() {

    return CANONICAL_NAME;
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Acid Slime",
        "${monster.acid_slime}",
        737,
        0,
        4,
        1666,
        36,
        81,
        30000L,
        "Slime@023",
        "Slimea@35",
        "SlimeC!z",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        64,
        198,
        java.util.List.of(),
        false,
        0.0f,
        51,
        47,
        47,
        58,
        0,
        47,
        0,
        new int[] {51, 102, 102, 51, 77, 5000, 100, 100, 100, 100, 100, 100},
        36,
        69,
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
        13,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d46+35", 442, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10091, 2, 18)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
