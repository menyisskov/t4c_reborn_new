package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemPotionOfMana {
  private ItemPotionOfMana() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_potion_of_mana}",
        "",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 233,
        null, 10062, 0, 1, 0,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "MANA"), new SpellData.T4cEffect.EffectParam(3, "25")))));
  }
}
