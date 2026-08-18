package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Hurricane {
  private Hurricane() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.hurricane}",
        "${spell.description.hurricane}",
        "20",
        8,
        105,
        102,
        88,
        true,
        true,
        "64kSpellIconAirAttackArea",
        "64kSpellEnergyBallYellow-",
        "GreatBolt-",
        0,
        0,
        "Healing.wav",
        "Spark.wav",
        0,
        "0",
        "0",
        249249,
        null,
        10143,
        3,
        19,
        1,
        "100",
        "2*(1000+if((1380-(self.level-88)*20)>=0?(1380-(self.level-88)*20):0))",
        "2*(750+if((1380-(self.level-88)*20)>=0?(1380-(self.level-88)*20):0))",
        "2*(750+if((1380-(self.level-88)*20)>=0?(1380-(self.level-88)*20):0))",
        30087,
        30087,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d159+75+self.int/14+self.wis/14)*self.air/target.r_air)"),
                    new SpellData.T4cEffect.EffectParam(
                        2,
                        "-(((1d159+75+self.int/14+self.wis/14)*self.air/target.r_air)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
