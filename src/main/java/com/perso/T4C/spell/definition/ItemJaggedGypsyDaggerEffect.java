package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemJaggedGypsyDaggerEffect {
  private ItemJaggedGypsyDaggerEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_jagged_gypsy_dagger_effect}",
        "${spell.description.item_jagged_gypsy_dagger_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "30000",
        "2500",
        233,
        null,
        10403,
        6,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10404"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
