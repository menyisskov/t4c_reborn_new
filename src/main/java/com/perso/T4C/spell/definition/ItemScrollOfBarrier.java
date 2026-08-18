package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfBarrier {
  private ItemScrollOfBarrier() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_barrier}",
        "${spell.description.item_scroll_of_barrier}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-", "BlueWipe-", 0, 0,
        "Healing.wav", "Mind Shield.wav", 0, "120000", "0", 233,
        null, 10656, 0, 5, 1,
        "100", "1000+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)", "750+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)", "750+if((710-(self.level-21)*20)>=0?(710-(self.level-21)*20):0)",
        30052, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, "TRUE"), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "(5+self.int/50+self.wis/25)"))), new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10123"), new SpellData.T4cEffect.EffectParam(2, "100")))));
  }
}
