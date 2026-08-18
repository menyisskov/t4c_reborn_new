package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Poison {
  private Poison() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.poison}",
        "${spell.description.poison}",
        "2",
        0,
        30,
        18,
        7,
        false,
        true,
        "64kSpellIconWaterAttackSingle",
        "64kSpellEnergyBallBlue-",
        "SmallPoisonCloud-",
        0,
        0,
        "Healing.wav",
        "Ice Cloud.wav",
        0,
        "20100",
        "1000",
        3017,
        null,
        10020,
        4,
        11,
        2,
        "100",
        "1000+if((570-(self.level-7)*20)>=0?(570-(self.level-7)*20):0)",
        "750+if((570-(self.level-7)*20)>=0?(570-(self.level-7)*20):0)",
        "750+if((570-(self.level-7)*20)>=0?(570-(self.level-7)*20):0)",
        30108,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10021"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null))),
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((self.int/21)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
