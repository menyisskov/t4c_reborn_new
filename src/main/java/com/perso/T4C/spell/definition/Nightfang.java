package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-150 dark single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Nightfang {
  private Nightfang() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "nightfang", 99936, HighTierSpellCurve.DARK, 150, Shape.BOLT);
  }
}
