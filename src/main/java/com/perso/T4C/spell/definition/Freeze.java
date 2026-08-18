package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Freeze {
  private Freeze() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.freeze}",
        "${spell.description.freeze}",
        "5",
        0,
        57,
        24,
        20,
        true,
        true,
        "64kSpellIconWaterAttackSingle",
        "64kSpellEnergyBallBlue-",
        "Freeze-",
        0,
        0,
        "Healing.wav",
        "Freeze.wav",
        0,
        "0",
        "0",
        20625,
        null,
        10025,
        4,
        11,
        2,
        "100",
        "1000+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        "750+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        "750+if((700-(self.level-20)*20)>=0?(700-(self.level-20)*20):0)",
        30080,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d20+21+self.int/16)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
