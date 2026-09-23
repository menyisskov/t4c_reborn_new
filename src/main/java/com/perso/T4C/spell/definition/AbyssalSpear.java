package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-250 water single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class AbyssalSpear {
  private AbyssalSpear() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "abyssal_spear", 99924, HighTierSpellCurve.WATER, 250, Shape.BOLT);
  }
}
