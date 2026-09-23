package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-400 dark area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class EclipseOfRuin {
  private EclipseOfRuin() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "eclipse_of_ruin", 99940, HighTierSpellCurve.DARK, 400, Shape.AREA);
  }
}
