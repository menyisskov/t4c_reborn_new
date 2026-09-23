package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/**
 * Level-150 light single-target attack of the high-tier spell ladder - see
 * {@link HighTierSpellCurve}. An older spell (same name and id) re-tiered onto the shared
 * curve in T4C-0025 so every school has a spell at the same levels.
 */
public final class Sunscour {
  private Sunscour() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "sunscour", 99904, HighTierSpellCurve.LIGHT, 150, Shape.BOLT);
  }
}
