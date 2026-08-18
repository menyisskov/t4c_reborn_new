package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemManaPrism {
  private ItemManaPrism() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_mana_prism}",
        "${spell.description.item_mana_prism}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterBoost",
        null, null, 0, 0,
        null, null, 0, "120000", "2000", 233,
        null, 10213, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10214"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
