package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemHyperPotionOfViolentRage {
  private ItemHyperPotionOfViolentRage() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_hyper_potion_of_violent_rage}",
        "${spell.description.item_hyper_potion_of_violent_rage}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "Curse-",
        null,
        0,
        0,
        "Curse.wav",
        null,
        0,
        "60000",
        "0",
        233,
        null,
        10183,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30015,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "str"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_str*3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_attack*3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "-self.AC"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-self.true_dodge*2")))));
  }
}
