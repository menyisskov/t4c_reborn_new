package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Tornado {
  private Tornado() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.tornado}",
        "${spell.description.tornado}",
        "18", 6, 96, 93, 79,
        true, true, "64kSpellIconAirAttackArea",
        "64kSpellEnergyBallYellow-", "GreatBolt-", 0, 0,
        "Healing.wav", "Spark.wav", 0, "0", "0", 208881,
        null, 10142, 3, 19, 1,
        "100", "2*(1000+if((1290-(self.level-79)*20)>=0?(1290-(self.level-79)*20):0))", "2*(750+if((1290-(self.level-79)*20)>=0?(1290-(self.level-79)*20):0))", "2*(750+if((1290-(self.level-79)*20)>=0?(1290-(self.level-79)*20):0))",
        30087, 30002, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d144+67+self.int/16+self.wis/16)*self.air/target.r_air)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d144+67+self.int/16+self.wis/16)*self.air/target.r_air)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
