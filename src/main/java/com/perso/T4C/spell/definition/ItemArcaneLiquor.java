package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemArcaneLiquor {
  private ItemArcaneLiquor() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_arcane_liquor}",
        "${spell.description.item_arcane_liquor}",
        "0", 0, 0, 0, 0,
        false, false, "64kIconPotion",
        null, null, 0, 0,
        null, null, 0, "600000", "0", 233,
        null, 10463, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "100"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "-(target.wis/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "int"), new SpellData.T4cEffect.EffectParam(3, "-(target.int/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "(self.true_r_air/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "(self.true_r_earth/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "(self.true_r_water/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "(self.true_r_dark/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "(self.true_r_fire/10)")))));
  }
}
