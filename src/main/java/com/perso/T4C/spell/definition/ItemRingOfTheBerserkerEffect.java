package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemRingOfTheBerserkerEffect {
  private ItemRingOfTheBerserkerEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_ring_of_the_berserker_effect}",
        "${spell.description.item_ring_of_the_berserker_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        null, null, 0, 0,
        null, null, 0, "120000", "0", 233,
        null, 10486, 1, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "strength"), new SpellData.T4cEffect.EffectParam(3, "self.true_str/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "self.true_attack/4")))));
  }
}
