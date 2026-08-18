package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfFury {
  private ItemPotionOfFury() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_fury}",
        "${spell.description.item_potion_of_fury}",
        "0", 0, 0, 0, 7,
        false, true, "64kSpellIconFireBoost",
        null, null, 0, 0,
        null, null, 0, "300000", "0", 2860,
        null, 10040, 0, 5, 2,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "Strength"), new SpellData.T4cEffect.EffectParam(3, "10")))));
  }
}
