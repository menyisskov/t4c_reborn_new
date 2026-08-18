package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemHyperPotionOfFortitude {
  private ItemHyperPotionOfFortitude() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_hyper_potion_of_fortitude}",
        "${spell.description.item_hyper_potion_of_fortitude}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthBoost",
        null, null, 0, 0,
        null, null, 0, "300000", "0", 233,
        null, 10181, 2, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "Endurance"), new SpellData.T4cEffect.EffectParam(3, "if(self.true_end/4<50?50:self.true_end/4)")))));
  }
}
