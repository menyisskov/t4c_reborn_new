package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemCloakOfRenewalEffect {
  private ItemCloakOfRenewalEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_cloak_of_renewal_effect}",
        "${spell.description.item_cloak_of_renewal_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconWaterBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "15000",
        "2500",
        233,
        null,
        10307,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10308"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "0")))));
  }
}
