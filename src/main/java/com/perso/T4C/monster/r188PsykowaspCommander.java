package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r188PsykowaspCommander extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r188PsykowaspCommander(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Commander",
        "${monster.psykowasp_commander}",
        463,
        0,
        3,
        810,
        22,
        50,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Wasp Attack.wav",
        "Wasp Dying.wav",
        "Wasp Hit.wav",
        44,
        137,
        java.util.List.of(new MonsterDef.LootDrop("Psykowasp nectar", 0.0069999998f)),
        false,
        0.0f,
        40,
        37,
        37,
        45,
        0,
        37,
        0,
        new int[] {54, 109, 82, 82, 82, 5000, 100, 100, 100, 100, 100, 100},
        25,
        110,
        0,
        1076363264,
        20029,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        100,
        45,
        0,
        true,
        java.util.List.of(new MonsterDef.Attack("1d29+21", 310, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
