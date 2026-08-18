package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Blizzard {
  private Blizzard() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.blizzard}",
        "${spell.description.blizzard}",
        "17", 5, 170, 48, 74,
        true, true, "64kSpellIconWaterAttackArea",
        "64kSpellEnergyBallBlue-", "64kSpellGlacier-", 0, 0,
        "Healing.wav", "Glacier.wav", 0, "0", "0", 187630,
        null, 10134, 4, 19, 2,
        "100", "2*(1000+if((1240-(self.level-74)*20)>=0?(1240-(self.level-74)*20):0))", "2*(750+if((1240-(self.level-74)*20)>=0?(1240-(self.level-74)*20):0))", "2*(750+if((1240-(self.level-74)*20)>=0?(1240-(self.level-74)*20):0))",
        30085, 30020, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d52+72+self.int/8)*self.water/target.r_water)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d52+72+self.int/8)*self.water/target.r_water)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
