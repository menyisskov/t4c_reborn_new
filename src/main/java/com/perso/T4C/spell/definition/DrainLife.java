package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DrainLife {
  private DrainLife() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.drain_life}",
        "${spell.description.drain_life}",
        "6", 0, 83, 16, 23,
        true, true, "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlack-", "Curse-", 0, 0,
        "Healing.wav", "Curse.wav", 0, "0", "0", 22057,
        null, 10152, 6, 11, 2,
        "100", "1000+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)", "750+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)", "750+if((730-(self.level-23)*20)>=0?(730-(self.level-23)*20):0)",
        30062, 0, true, List.of(new SpellData.T4cEffect(10, List.of(new SpellData.T4cEffect.EffectParam(1, "if(target.r_dark=5025?0:-((1d19+17+self.int/30)*self.dark/target.r_dark))"), new SpellData.T4cEffect.EffectParam(2, "0"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
