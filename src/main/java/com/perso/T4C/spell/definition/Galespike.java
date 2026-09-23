package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-150 air single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Galespike {
  private Galespike() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "galespike", 99931, HighTierSpellCurve.AIR, 150, Shape.BOLT);
  }
}
