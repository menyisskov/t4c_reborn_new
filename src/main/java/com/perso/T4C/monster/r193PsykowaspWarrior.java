package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r193PsykowaspWarrior extends DataMonster {
  public static final String SOUND_ATTACK = "Wasp Attack.wav";
  public static final String SOUND_DEATH = "Wasp Dying.wav";
  public static final String SOUND_HIT = "Wasp Hit.wav";

  public r193PsykowaspWarrior(MonsterDef definition, float worldX, float worldY)
      throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Psykowasp Warrior",
        "${monster.psykowasp_warrior}",
        254,
        0,
        2,
        337,
        13,
        29,
        30000L,
        "GiantWasp#h",
        "GiantWaspA#h",
        "GiantWaspC#k",
        "Wasp Attack.wav",
        "Wasp Dying.wav",
        "Wasp Hit.wav",
        26,
        82,
        java.util.List.of(new MonsterDef.LootDrop("Psykowasp nectar", 0.004f)),
        false,
        0.0f,
        30,
        28,
        28,
        33,
        0,
        28,
        0,
        new int[] {56, 115, 86, 86, 86, 5000, 100, 100, 100, 100, 100, 100},
        15,
        70,
        0,
        1075576832,
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
        java.util.List.of(new MonsterDef.Attack("1d17+12", 190, 100, 0, 0, 0)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
