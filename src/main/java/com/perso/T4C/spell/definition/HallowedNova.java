package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-350 light area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class HallowedNova {
  private HallowedNova() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "hallowed_nova", 99944, HighTierSpellCurve.LIGHT, 350, Shape.AREA);
  }
}
