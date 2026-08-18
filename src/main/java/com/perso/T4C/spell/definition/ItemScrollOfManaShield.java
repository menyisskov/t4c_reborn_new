package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfManaShield {
  private ItemScrollOfManaShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_mana_shield}",
        "${spell.description.item_scroll_of_mana_shield}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterDefense",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "120000", "0", 233,
        null, 10663, 4, 5, 1,
        "100", "1000+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)", "750+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)", "750+if((790-(self.level-29)*20)>=0?(790-(self.level-29)*20):0)",
        30004, 0, false, List.of(new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10146"), new SpellData.T4cEffect.EffectParam(2, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_dark/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_earth/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_fire/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_air/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_water/3")))));
  }
}
