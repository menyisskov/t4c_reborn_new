package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class IceStorm {
  private IceStorm() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ice_storm}",
        "${spell.description.ice_storm}",
        "15", 6, 152, 44, 65,
        true, true, "64kSpellIconWaterAttackArea",
        "64kSpellEnergyBallBlue-", "Freeze-", 0, 0,
        "Healing.wav", "Freeze.wav", 0, "0", "0", 151580,
        null, 10133, 4, 19, 1,
        "100", "2*(1000+if((1150-(self.level-65)*20)>=0?(1150-(self.level-65)*20):0))", "2*(750+if((1150-(self.level-65)*20)>=0?(1150-(self.level-65)*20):0))", "2*(750+if((1150-(self.level-65)*20)>=0?(1150-(self.level-65)*20):0))",
        30080, 30023, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d83+75+self.int/9)*self.water/target.r_water)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d83+75+self.int/9)*self.water/target.r_water)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
