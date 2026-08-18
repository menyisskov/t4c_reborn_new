package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class GreaterDrain {
  private GreaterDrain() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.greater_drain}",
        "${spell.description.greater_drain}",
        "10",
        0,
        136,
        16,
        41,
        true,
        true,
        "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "0",
        "0",
        70497,
        null,
        10169,
        6,
        11,
        2,
        "100",
        "1000+if((910-(self.level-41)*20)>=0?(910-(self.level-41)*20):0)",
        "750+if((910-(self.level-41)*20)>=0?(910-(self.level-41)*20):0)",
        "750+if((910-(self.level-41)*20)>=0?(910-(self.level-41)*20):0)",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                10,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1,
                        "if(target.r_dark=5025?0:-((1d28+30+self.int/24)*self.dark/target.r_dark))"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
