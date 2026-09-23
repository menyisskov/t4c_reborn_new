package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-300 light single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class SeraphsVerdict {
  private SeraphsVerdict() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "seraphs_verdict", 99943, HighTierSpellCurve.LIGHT, 300, Shape.BOLT);
  }
}
