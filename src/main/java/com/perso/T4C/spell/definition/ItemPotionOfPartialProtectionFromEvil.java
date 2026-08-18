package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfPartialProtectionFromEvil {
  private ItemPotionOfPartialProtectionFromEvil() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_partial_protection_from_evil}",
        "${spell.description.item_potion_of_partial_protection_from_evil}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconLightDefense",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "420000",
        "0",
        233,
        null,
        10288,
        0,
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
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_r_dark/2")))));
  }
}
