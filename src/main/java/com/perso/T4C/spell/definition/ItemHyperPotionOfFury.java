package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemHyperPotionOfFury {
  private ItemHyperPotionOfFury() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_hyper_potion_of_fury}",
        "${spell.description.item_hyper_potion_of_fury}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "300000",
        "0",
        233,
        null,
        10179,
        1,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "Strength"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "if(self.true_str/4<50?50:self.true_str/4)")))));
  }
}
