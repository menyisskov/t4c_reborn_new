package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-350 dark area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class UmbralTide {
  private UmbralTide() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "umbral_tide", 99939, HighTierSpellCurve.DARK, 350, Shape.AREA);
  }
}
