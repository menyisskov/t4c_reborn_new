package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Avalanche {
  private Avalanche() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.avalanche}",
        "${spell.description.avalanche}",
        "19",
        6,
        187,
        52,
        82,
        true,
        true,
        "64kSpellIconWaterAttackArea",
        "64kSpellEnergyBallBlue-",
        "64kSpellGlacier-",
        0,
        0,
        "Healing.wav",
        "Glacier.wav",
        0,
        "0",
        "0",
        222039,
        null,
        10135,
        4,
        19,
        1,
        "100",
        "2*(1000+if((1320-(self.level-82)*20)>=0?(1320-(self.level-82)*20):0))",
        "2*(750+if((1320-(self.level-82)*20)>=0?(1320-(self.level-82)*20):0))",
        "2*(750+if((1320-(self.level-82)*20)>=0?(1320-(self.level-82)*20):0))",
        30085,
        30030,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d101+94+self.int/8)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d101+94+self.int/8)*self.water/target.r_water)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
