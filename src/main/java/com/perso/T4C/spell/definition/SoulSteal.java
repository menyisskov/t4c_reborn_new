package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class SoulSteal {
  private SoulSteal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.soul_steal}",
        "${spell.description.soul_steal}",
        "16",
        0,
        215,
        17,
        68,
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
        163275,
        null,
        10264,
        6,
        11,
        2,
        "100",
        "1000+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        "750+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        "750+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                10,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1,
                        "if(target.r_dark=5025?0:-((1d44+48+self.int/18)*self.dark/target.r_dark))"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
