package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-300 water single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Tidebreaker {
  private Tidebreaker() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "tidebreaker", 99925, HighTierSpellCurve.WATER, 300, Shape.BOLT);
  }
}
