package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r203RobinHood extends DataMonster {
  public static final String SOUND_ATTACK = "Beast Attack.wav";
  public static final String SOUND_DEATH = "Beast Dying.wav";
  public static final String SOUND_HIT = "Beast Hit.wav";

  public r203RobinHood(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Robin Hood",
        "${monster.robin_hood}",
        5000,
        0,
        1,
        1,
        20,
        46,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        1,
        1,
        java.util.List.of(),
        false,
        0.0f,
        100,
        100,
        100,
        100,
        0,
        100,
        0,
        new int[] {100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100, 100},
        100,
        100,
        0,
        1079574528,
        10011,
        40023,
        40595,
        40020,
        40211,
        40027,
        41267,
        0,
        40215,
        100,
        1,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d27+19", 286, 100, 0, 0, 12)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
