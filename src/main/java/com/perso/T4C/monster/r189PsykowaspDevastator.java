package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r189PsykowaspDevastator extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r189PsykowaspDevastator(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Devastator",
        "${monster.psykowasp_devastator}",
        396,
        0,
        3,
        641,
        19,
        43,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        SOUND_ATTACK,
        SOUND_DEATH,
        SOUND_HIT,
        39,
        121,
        java.util.List.of(
            new MonsterDef.LootDrop("Devastator eyes", 0.03f),
            new MonsterDef.LootDrop("Psykowasp nectar", 0.006f)),
        false,
        0.0f,
        37,
        34,
        34,
        41,
        0,
        34,
        0,
        new int[] {55, 111, 83, 83, 83, 5000, 100, 100, 100, 100, 100, 100},
        22,
        98,
        0,
        1076232192,
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
        java.util.List.of(new MonsterDef.Attack("1d25+18", 274, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
