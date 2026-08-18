package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfClearThought {
  private ItemPotionOfClearThought() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_clear_thought}",
        "${spell.description.item_potion_of_clear_thought}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterBoost",
        null, null, 0, 0,
        null, null, 0, "300000", "0", 233,
        null, 10153, 0, 5, 2,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "Intelligence"), new SpellData.T4cEffect.EffectParam(3, "10")))));
  }
}
