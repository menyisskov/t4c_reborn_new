package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r191PsykowaspScout extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r191PsykowaspScout(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Scout",
        "${monster.psykowasp_scout}",
        217,
        0,
        2,
        267,
        11,
        25,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        23,
        71,
        java.util.List.of(new MonsterDef.LootDrop("Psykowasp nectar", 0.003f)),
        false,
        0.0f,
        28,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {58, 116, 87, 87, 87, 5000, 100, 100, 100, 100, 100, 100},
        13,
        62,
        0,
        1075314688,
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
        java.util.List.of(new MonsterDef.Attack("1d15+10", 166, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
