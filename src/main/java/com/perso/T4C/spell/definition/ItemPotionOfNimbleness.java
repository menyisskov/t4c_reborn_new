package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfNimbleness {
  private ItemPotionOfNimbleness() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_nimbleness}",
        "${spell.description.item_potion_of_nimbleness}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirBoost",
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
        10159,
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
                    new SpellData.T4cEffect.EffectParam(2, "agility"),
                    new SpellData.T4cEffect.EffectParam(3, "10")))));
  }
}
