package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Tsunami {
  private Tsunami() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.tsunami}",
        "${spell.description.tsunami}",
        "22", 0, 217, 58, 96,
        true, true, "64kSpellIconWaterAttackArea",
        "64kSpellEnergyBallBlue-", "64kSpellGlacier-", 0, 0,
        "Healing.wav", "Glacier.wav", 0, "0", "0", 287334,
        null, 10136, 4, 11, 2,
        "100", "1000+if((1460-(self.level-96)*20)>=0?(1460-(self.level-96)*20):0)", "750+if((1460-(self.level-96)*20)>=0?(1460-(self.level-96)*20):0)", "750+if((1460-(self.level-96)*20)>=0?(1460-(self.level-96)*20):0)",
        30085, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d65+93+self.int/7)*self.water/target.r_water)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
