package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-350 water area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class DrowningDeep {
  private DrowningDeep() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "drowning_deep", 99926, HighTierSpellCurve.WATER, 350, Shape.AREA);
  }
}
