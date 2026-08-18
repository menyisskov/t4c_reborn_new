package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Vaporize {
  private Vaporize() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.vaporize}",
        "${spell.description.vaporize}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconNoneAttackSingle",
        "64kSpellEnergyBallYellow-", "Freeze-", 0, 0,
        "Healing.wav", "Freeze.wav", 0, "0", "0", 233,
        null, 10210, 0, 0, 1,
        "100", "0", "0", "0",
        30081, 0, false, List.of(new SpellData.T4cEffect(12, List.of())));
  }
}
