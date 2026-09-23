package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-300 air single-target attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class TempestLance {
  private TempestLance() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "tempest_lance", 99934, HighTierSpellCurve.AIR, 300, Shape.BOLT);
  }
}
