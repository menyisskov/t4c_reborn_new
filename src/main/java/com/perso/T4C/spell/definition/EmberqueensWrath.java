package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/**
 * Level-350 fire area attack of the high-tier spell ladder - see
 * {@link HighTierSpellCurve}. An older spell (same name and id) re-tiered onto the shared
 * curve in T4C-0025 so every school has a spell at the same levels.
 */
public final class EmberqueensWrath {
  private EmberqueensWrath() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "emberqueens_wrath", 99909, HighTierSpellCurve.FIRE, 350, Shape.AREA);
  }
}
