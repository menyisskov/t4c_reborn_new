package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceBolt {
  private IceBolt() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_bolt}",
        "${spell.description.ice_bolt}",
        "6", 0, 68, 26, 25,
        true, true, "64kSpellIconWaterAttackSingle",
        "IceShard", "IceCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0, "0", "0", 30418,
        null, 10026, 4, 11, 1,
        "100", "1000+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)", "750+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)", "750+if((750-(self.level-25)*20)>=0?(750-(self.level-25)*20):0)",
        30023, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d39+31+self.int/15)*self.water/target.r_water)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
