package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-200 air area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Thunderhead {
  private Thunderhead() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "thunderhead", 99932, HighTierSpellCurve.AIR, 200, Shape.AREA);
  }
}
