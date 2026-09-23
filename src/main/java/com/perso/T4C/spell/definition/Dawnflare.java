package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-200 light area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Dawnflare {
  private Dawnflare() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "dawnflare", 99941, HighTierSpellCurve.LIGHT, 200, Shape.AREA);
  }
}
