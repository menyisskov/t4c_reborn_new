package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfTrueSight {
  private ItemScrollOfTrueSight() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_true_sight}",
        "${spell.description.item_scroll_of_true_sight}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
        "GreenWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "300000",
        "0",
        233,
        null,
        10399,
        2,
        5,
        1,
        "100",
        "1000+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)",
        "750+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)",
        "750+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)",
        30003,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10262"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                16,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, null))),
            new SpellData.T4cEffect(
                17,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, null))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10677"),
                    new SpellData.T4cEffect.EffectParam(2, "100")))));
  }
}
