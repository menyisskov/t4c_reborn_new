package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-300 earth single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class MountainsFist {
  private MountainsFist() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "mountains_fist", 99928, HighTierSpellCurve.EARTH, 300, Shape.BOLT);
  }
}
