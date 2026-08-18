package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ChainLightning {
  private ChainLightning() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.chain_lightning}",
        "${spell.description.chain_lightning}",
        "7", 5, 46, 45, 30,
        true, true, "64kSpellIconAirAttackArea",
        "Lightning", "ElectricShield-", 0, 0,
        "Lightning.wav", "Electric Shield.wav", 0, "100", "100", 41584,
        null, 10078, 3, 19, 2,
        "100", "2*(1000+if((800-(self.level-30)*20)>=0?(800-(self.level-30)*20):0))", "2*(750+if((800-(self.level-30)*20)>=0?(800-(self.level-30)*20):0))", "2*(750+if((800-(self.level-30)*20)>=0?(800-(self.level-30)*20):0))",
        30002, 30002, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d41+23+self.int/28+self.wis/28)*self.air/target.r_air)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d41+23+self.int/28+self.wis/28)*self.air/target.r_air)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
