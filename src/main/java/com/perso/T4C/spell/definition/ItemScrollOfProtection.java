package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfProtection {
  private ItemScrollOfProtection() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_protection}",
        "${spell.description.item_scroll_of_protection}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthDefense",
        "GreenWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "180000", "0", 233,
        null, 10655, 2, 5, 1,
        "100", "1000+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)", "750+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)", "750+if((580-(self.level-8)*20)>=0?(580-(self.level-8)*20):0)",
        30003, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "(3+self.int/100+self.wis/50)"))), new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10115"), new SpellData.T4cEffect.EffectParam(2, "100")))));
  }
}
