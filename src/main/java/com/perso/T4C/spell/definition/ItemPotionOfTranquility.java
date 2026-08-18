package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfTranquility {
  private ItemPotionOfTranquility() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_tranquility}",
        "${spell.description.item_potion_of_tranquility}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconLightBoost",
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
        10158,
        0,
        5,
        2,
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
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "Wisdom"),
                    new SpellData.T4cEffect.EffectParam(3, "10")))));
  }
}
