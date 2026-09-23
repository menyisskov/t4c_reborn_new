package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-200 water area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Frostgale {
  private Frostgale() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "frostgale", 99923, HighTierSpellCurve.WATER, 200, Shape.AREA);
  }
}
