package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfResistFire {
  private ItemScrollOfResistFire() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_resist_fire}",
        "${spell.description.item_scroll_of_resist_fire}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterDefense",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "60000", "0", 233,
        null, 10658, 4, 5, 1,
        "100", "1000+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)", "750+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)", "750+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)",
        30004, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "self.true_r_fire"))), new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10154"), new SpellData.T4cEffect.EffectParam(2, "100")))));
  }
}
