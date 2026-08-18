package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfLightHealing {
  private ItemPotionOfLightHealing() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_light_healing}",
        "",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 233,
        null, 10176, 2, 1, 0,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "25"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
