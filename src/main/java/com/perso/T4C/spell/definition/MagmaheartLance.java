package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-250 fire single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class MagmaheartLance {
  private MagmaheartLance() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "magmaheart_lance", 99919, HighTierSpellCurve.FIRE, 250, Shape.BOLT);
  }
}
