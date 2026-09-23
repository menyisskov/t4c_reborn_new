package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/**
 * Level-200 earth area attack of the high-tier spell ladder - see
 * {@link HighTierSpellCurve}. An older spell (same name and id) re-tiered onto the shared
 * curve in T4C-0025 so every school has a spell at the same levels.
 */
public final class LandSlide {
  private LandSlide() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "land_slide", 99901, HighTierSpellCurve.EARTH, 200, Shape.AREA);
  }
}
