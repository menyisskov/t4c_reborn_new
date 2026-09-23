package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-150 water single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class RimeLance {
  private RimeLance() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "rime_lance", 99922, HighTierSpellCurve.WATER, 150, Shape.BOLT);
  }
}
