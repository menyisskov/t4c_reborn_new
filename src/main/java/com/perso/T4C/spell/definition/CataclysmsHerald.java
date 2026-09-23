package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/**
 * Level-400 water area attack of the high-tier spell ladder - see
 * {@link HighTierSpellCurve}. An older spell (same name and id) re-tiered onto the shared
 * curve in T4C-0025 so every school has a spell at the same levels.
 */
public final class CataclysmsHerald {
  private CataclysmsHerald() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "cataclysms_herald", 99916, HighTierSpellCurve.WATER, 400, Shape.AREA);
  }
}
