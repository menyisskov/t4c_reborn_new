package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-400 earth area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class TectonicRuin {
  private TectonicRuin() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "tectonic_ruin", 99930, HighTierSpellCurve.EARTH, 400, Shape.AREA);
  }
}
