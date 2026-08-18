package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfFortitude {
  private ItemPotionOfFortitude() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_fortitude}",
        "${spell.description.item_potion_of_fortitude}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
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
        10156,
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
                    new SpellData.T4cEffect.EffectParam(2, "Endurance"),
                    new SpellData.T4cEffect.EffectParam(3, "10")))));
  }
}
