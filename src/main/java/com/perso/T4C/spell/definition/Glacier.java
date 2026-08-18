package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Glacier {
  private Glacier() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.glacier}",
        "${spell.description.glacier}",
        "13",
        0,
        130,
        40,
        55,
        true,
        true,
        "64kSpellIconWaterAttackSingle",
        "64kSpellEnergyBallBlue-",
        "64kSpellGlacier-",
        0,
        0,
        "Healing.wav",
        "Glacier.wav",
        0,
        "0",
        "0",
        110825,
        null,
        10132,
        4,
        11,
        1,
        "100",
        "1000+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        "750+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        "750+if((1050-(self.level-55)*20)>=0?(1050-(self.level-55)*20):0)",
        30085,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d72+64+self.int/10)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
