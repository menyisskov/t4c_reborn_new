package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-300 fire single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class SunforgeBrand {
  private SunforgeBrand() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "sunforge_brand", 99920, HighTierSpellCurve.FIRE, 300, Shape.BOLT);
  }
}
