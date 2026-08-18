package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfLesserWaterResistance {
  private ItemPotionOfLesserWaterResistance() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_lesser_water_resistance}",
        "${spell.description.item_potion_of_lesser_water_resistance}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireDefense",
        null, null, 0, 0,
        null, null, 0, "240000", "0", 233,
        null, 10280, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "self.true_r_water/4")))));
  }
}
