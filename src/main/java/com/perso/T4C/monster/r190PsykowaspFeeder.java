package com.perso.T4C.monster;

import java.util.Map;
import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.DataMonster;
import com.perso.T4C.monster.core.MonsterDef;

public final class r190PsykowaspFeeder extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r190PsykowaspFeeder(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Feeder",
        "${monster.psykowasp_feeder}",
        200,
        0,
        3,
        328,
        12,
        36,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC!k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        25,
        77,
        java.util.List.of(
            new MonsterDef.LootDrop("Feeder eyes", 0.05f),
            new MonsterDef.LootDrop("Psykowasp nectar", 0.003f)),
        false,
        0.0f,
        29,
        27,
        27,
        31,
        0,
        27,
        0,
        new int[] {58, 116, 87, 87, 87, 5000, 100, 100, 100, 100, 100, 100},
        14,
        66,
        0,
        1075052544,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        75,
        45,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d25+11", 178, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 0, 10091, 1, 10),
            new MonsterDef.Attack("", 0, 0, 10357, 1, 10)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
