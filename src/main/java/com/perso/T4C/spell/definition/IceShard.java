package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceShard {
  private IceShard() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_shard}",
        "${spell.description.ice_shard}",
        "3", 0, 34, 19, 9,
        true, true, "64kSpellIconWaterAttackSingle",
        "IceShard", "IceCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0, "0", "0", 17200,
        null, 10022, 4, 11, 1,
        "100", "1000+if((590-(self.level-9)*20)>=0?(590-(self.level-9)*20):0)", "750+if((590-(self.level-9)*20)>=0?(590-(self.level-9)*20):0)", "750+if((590-(self.level-9)*20)>=0?(590-(self.level-9)*20):0)",
        30023, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d21+13+self.int/20)*self.water/target.r_water)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
