package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemFineWine {
  private ItemFineWine() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_fine_wine}",
        "${spell.description.item_fine_wine}",
        "0", 0, 0, 0, 0,
        false, false, "64kIconPotion",
        null, null, 0, 0,
        null, null, 0, "300000", "0", 233,
        null, 10462, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "50"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "int"), new SpellData.T4cEffect.EffectParam(3, "-(target.int/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "-(target.wis/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "Strength"), new SpellData.T4cEffect.EffectParam(3, "(self.true_str/4)")))));
  }
}
