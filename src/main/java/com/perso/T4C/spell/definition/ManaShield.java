package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ManaShield {
  private ManaShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mana_shield}",
        "${spell.description.mana_shield}",
        "14",
        0,
        76,
        28,
        29,
        false,
        true,
        "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-",
        "BlueWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "120000",
        "0",
        39246,
        null,
        10146,
        4,
        3,
        1,
        "100",
        "1000+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)",
        "750+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)",
        "750+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)",
        30052,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_r_dark/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_r_fire/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_r_earth/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_r_water/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_r_air/3")))));
  }
}
