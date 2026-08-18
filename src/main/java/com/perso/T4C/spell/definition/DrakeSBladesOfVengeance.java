package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DrakeSBladesOfVengeance {
  private DrakeSBladesOfVengeance() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.drake_s_blades_of_vengeance}",
        "${spell.description.drake_s_blades_of_vengeance}",
        "0", 25, 0, 0, 0,
        false, false, "64kSpellIconNoneAttackArea",
        "64kSpellEnergyBall-", "64kSpellFireCircle-", 0, 0,
        "Healing.wav", "Fire circle.wav", 0, "0", "0", 233,
        null, 10215, 0, 18, 2,
        "100", "0", "0", "0",
        30121, 30124, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "0"), new SpellData.T4cEffect.EffectParam(2, "-(self.int*self.int)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
