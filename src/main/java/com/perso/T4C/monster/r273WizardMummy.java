package com.perso.T4C.monster;

import com.perso.T4C.exception.GameException;
import com.perso.T4C.monster.core.*;

public final class r273WizardMummy extends DataMonster {
  public static final String SOUND_ATTACK = "Mummy Attack.wav";
  public static final String SOUND_DEATH = "Mummy Dying.wav";
  public static final String SOUND_HIT = "Mummy Hit.wav";

  public r273WizardMummy(MonsterDef definition, float worldX, float worldY) throws GameException {

    super(definition, worldX, worldY);
  }

  public static MonsterDef definition() {
    return new MonsterDef(
        "Wizard Mummy",
        "${monster.wizard_mummy}",
        217,
        0,
        2,
        267,
        11,
        25,
        30000L,
        "Mummy#i",
        "MummyA#j",
        "MummyC",
        "Mummy Attack.wav",
        "Mummy Dying.wav",
        "Mummy Hit.wav",
        23,
        71,
        java.util.List.of(new MonsterDef.LootDrop("Torch", 0.05f)),
        false,
        0.0f,
        28,
        26,
        26,
        30,
        0,
        26,
        0,
        new int[] {87, 87, 116, 58, 5025, 58, 100, 100, 100, 100, 100, 100},
        13,
        62,
        0,
        1075314688,
        20011,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        0,
        50,
        21,
        0,
        true,
        java.util.List.of(
            new MonsterDef.Attack("1d15+10", 166, 100, 0, 0, 0),
            new MonsterDef.Attack("", 0, 100, 10120, 5, 15)),
        false,
        0,
        java.util.List.of(),
        java.util.Map.of());
  }
}
