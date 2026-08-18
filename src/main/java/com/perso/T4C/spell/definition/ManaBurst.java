package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ManaBurst {
  private ManaBurst() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mana_burst}",
        "${spell.description.mana_burst}",
        "8",
        0,
        63,
        39,
        32,
        true,
        true,
        "64kSpellIconNoneAttackSingle",
        "64kSpellEnergyBallPurple-",
        "SmallPoisonCloud-",
        0,
        0,
        "Healing.wav",
        "Ice Cloud.wav",
        0,
        "0",
        "0",
        46410,
        null,
        10131,
        0,
        11,
        2,
        "100",
        "500+if((820-(self.level-32)*20)>=0?(820-(self.level-32)*20):0)",
        "375+if((820-(self.level-32)*20)>=0?(820-(self.level-32)*20):0)",
        "375+if((820-(self.level-32)*20)>=0?(820-(self.level-32)*20):0)",
        30110,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "if(target.r_air<4000?-(1d26+33+self.int/13):0)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
