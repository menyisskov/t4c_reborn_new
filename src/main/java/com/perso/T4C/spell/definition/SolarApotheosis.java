package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-400 light area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class SolarApotheosis {
  private SolarApotheosis() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "solar_apotheosis", 99945, HighTierSpellCurve.LIGHT, 400, Shape.AREA);
  }
}
