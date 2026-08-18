package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class VortexOfAir {
  private VortexOfAir() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.vortex_of_air}",
        "${spell.description.vortex_of_air}",
        "10",
        0,
        56,
        55,
        40,
        true,
        true,
        "64kSpellIconAirAttackSingle",
        "64kSpellEnergyBallYellow-",
        "GreatBolt-",
        0,
        0,
        "Healing.wav",
        "Spark.wav",
        0,
        "0",
        "0",
        67637,
        null,
        10080,
        3,
        11,
        2,
        "100",
        "1000+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0)",
        "750+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0)",
        "750+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0)",
        30087,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d52+30+self.int/24+self.wis/24)*self.air/target.r_air)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
