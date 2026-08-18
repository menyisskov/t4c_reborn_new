package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Plague {
  private Plague() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.plague}",
        "${spell.description.plague}",
        "40",
        0,
        112,
        16,
        33,
        false,
        true,
        "64kSpellIconDarkAttackSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "300000",
        "6000",
        48897,
        null,
        10107,
        6,
        11,
        1,
        "100",
        "1000+if((830-(self.level-33)*20)>=0?(830-(self.level-33)*20):0)",
        "750+if((830-(self.level-33)*20)>=0?(830-(self.level-33)*20):0)",
        "750+if((830-(self.level-33)*20)>=0?(830-(self.level-33)*20):0)",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "-(target.light/50)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10105"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "if(target.r_dark<=1000?100:0)"),
                    new SpellData.T4cEffect.EffectParam(4, null))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "max hp"),
                    new SpellData.T4cEffect.EffectParam(3, "-target.maxhp/4")))));
  }
}
