package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-150 fire single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class Scorchbrand {
  private Scorchbrand() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "scorchbrand", 99917, HighTierSpellCurve.FIRE, 150, Shape.BOLT);
  }
}
