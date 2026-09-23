package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-150 earth single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Stonefang {
  private Stonefang() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "stonefang", 99927, HighTierSpellCurve.EARTH, 150, Shape.BOLT);
  }
}
