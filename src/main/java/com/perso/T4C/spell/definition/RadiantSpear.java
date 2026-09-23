package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-250 light single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class RadiantSpear {
  private RadiantSpear() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "radiant_spear", 99942, HighTierSpellCurve.LIGHT, 250, Shape.BOLT);
  }
}
