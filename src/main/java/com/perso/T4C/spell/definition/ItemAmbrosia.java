package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemAmbrosia {
  private ItemAmbrosia() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_ambrosia}",
        "${spell.description.item_ambrosia}",
        "0", 0, 0, 0, 0,
        false, false, "64kIconPotion",
        null, null, 0, 0,
        null, null, 0, "1200000", "0", 233,
        null, 10464, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "250"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "-(target.wis/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "int"), new SpellData.T4cEffect.EffectParam(3, "-(target.int/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "air"), new SpellData.T4cEffect.EffectParam(3, "(self.true_air/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "earth"), new SpellData.T4cEffect.EffectParam(3, "(self.true_earth/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dark"), new SpellData.T4cEffect.EffectParam(3, "(self.true_dark/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "light"), new SpellData.T4cEffect.EffectParam(3, "(self.true_light/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "water"), new SpellData.T4cEffect.EffectParam(3, "(self.true_water/10)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "fire"), new SpellData.T4cEffect.EffectParam(3, "(self.true_fire/10)")))));
  }
}
