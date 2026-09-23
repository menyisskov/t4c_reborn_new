package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.HighTierSpellCurve.Shape;
import com.perso.T4C.spell.SpellData;

/** Level-350 earth area attack of the high-tier spell ladder - see {@link HighTierSpellCurve}. */
public final class WorldrootUpheaval {
  private WorldrootUpheaval() {}

  public static SpellData definition() {
    return HighTierSpellCurve.attack(
        "worldroot_upheaval", 99929, HighTierSpellCurve.EARTH, 350, Shape.AREA);
  }
}
