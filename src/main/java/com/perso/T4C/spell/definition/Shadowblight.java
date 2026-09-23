package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-200 dark area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Shadowblight {
  private Shadowblight() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "shadowblight", 99937, HighTierSpellCurve.DARK, 200, Shape.AREA);
  }
}
