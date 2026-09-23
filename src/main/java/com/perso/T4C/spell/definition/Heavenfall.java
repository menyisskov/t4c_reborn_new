package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-400 air area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Heavenfall {
  private Heavenfall() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "heavenfall", 99935, HighTierSpellCurve.AIR, 400, Shape.AREA);
  }
}
