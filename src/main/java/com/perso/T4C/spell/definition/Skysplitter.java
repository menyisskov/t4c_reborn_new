package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-250 air single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Skysplitter {
  private Skysplitter() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "skysplitter", 99933, HighTierSpellCurve.AIR, 250, Shape.BOLT);
  }
}
