package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/**
 * Level-350 air area attack of the high-tier spell ladder - see
 * {@link HighTierSpellCurve}. An older spell (same name and id) re-tiered onto the shared
 * curve in T4C-0025 so every school has a spell at the same levels.
 */
public final class StormcallersJudgment {
  private StormcallersJudgment() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "stormcallers_judgment", 99907, HighTierSpellCurve.AIR, 350, Shape.AREA);
  }
}
