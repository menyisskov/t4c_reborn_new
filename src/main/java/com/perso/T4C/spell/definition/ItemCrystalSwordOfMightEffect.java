package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemCrystalSwordOfMightEffect {
  private ItemCrystalSwordOfMightEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_crystal_sword_of_might_effect}",
        "${spell.description.item_crystal_sword_of_might_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterBoost",
        null, null, 0, 0,
        null, null, 0, "10000", "1000", 233,
        null, 10437, 4, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10438"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
