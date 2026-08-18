package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemVoyeurCloakEffect {
  private ItemVoyeurCloakEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_voyeur_cloak_effect}",
        "${spell.description.item_voyeur_cloak_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirDefense",
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
        10647,
        3,
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
                15,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30015")))));
  }
}
