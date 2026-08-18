package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemMantleOfInfiniteBlessingsEffect {
  private ItemMantleOfInfiniteBlessingsEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_mantle_of_infinite_blessings_effect}",
        "${spell.description.item_mantle_of_infinite_blessings_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconLightBoost",
        "HealingSpell-", null, 0, 0,
        "Healing.wav", null, 0, "30000", "0", 233,
        null, 10306, 0, 5, 1,
        "100", "0", "0", "0",
        30001, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "max hp"), new SpellData.T4cEffect.EffectParam(3, "1d(self.wis/8)+self.wis/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "self.wis/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "self.wis/4")))));
  }
}
