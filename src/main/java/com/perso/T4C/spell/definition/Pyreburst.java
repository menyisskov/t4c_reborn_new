package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-200 fire area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Pyreburst {
  private Pyreburst() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "pyreburst", 99918, HighTierSpellCurve.FIRE, 200, Shape.AREA);
  }
}
