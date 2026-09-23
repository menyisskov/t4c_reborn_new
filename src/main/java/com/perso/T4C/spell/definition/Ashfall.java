package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-400 fire area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Ashfall {
  private Ashfall() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "ashfall", 99921, HighTierSpellCurve.FIRE, 400, Shape.AREA);
  }
}
