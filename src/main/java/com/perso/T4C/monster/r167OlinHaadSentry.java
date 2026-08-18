package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r167OlinHaadSentry extends DataMonster {
  public static final String SOUND_ATTACK = "Electrik.wav";
  public static final String SOUND_DEATH = "Tree Ent Dying.wav";
  public static final String SOUND_HIT = "AxeWood.wav";

  public r167OlinHaadSentry(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Olin Haad Sentry",
        "${monster.olin_haad_sentry}",
        581,
        0,
        3,
        1154,
        28,
        63,
        30000L,
        "",
        null,
        null,
        "Beast Attack.wav",
        "Beast Dying.wav",
        "Beast Hit.wav",
        53,
        165,
        java.util.List.of(),
        false,
        0.0f,
        45,
        41,
        41,
        51,
        0,
        41,
        0,
        new int[] {79, 79, 79, 79, 53, 5000, 100, 100, 100, 100, 100, 100},
        30,
        130,
        0,
        1076756480,
        10011,
        40030,
        40595,
        40200,
        40201,
        40202,
        40062,
        40175,
        40215,
        100,
        20,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d36+27", 370, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
