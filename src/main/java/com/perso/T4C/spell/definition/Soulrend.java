package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-250 dark single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Soulrend {
  private Soulrend() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "soulrend", 99938, HighTierSpellCurve.DARK, 250, Shape.BOLT);
  }
}
