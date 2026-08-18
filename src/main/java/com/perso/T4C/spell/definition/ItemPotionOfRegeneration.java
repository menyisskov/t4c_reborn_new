package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfRegeneration {
  private ItemPotionOfRegeneration() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_regeneration}",
        "${spell.description.item_potion_of_regeneration}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconLightHealSingle",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "120000",
        "2000",
        233,
        null,
        10110,
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
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10113"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
