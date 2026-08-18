package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfStoneSkin {
  private ItemScrollOfStoneSkin() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_stone_skin}",
        "${spell.description.item_scroll_of_stone_skin}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthDefense",
        "RockyFly-", null, 0, 0,
        "Rocks Fly.wav", null, 0, "300000", "0", 233,
        null, 10660, 2, 5, 1,
        "100", "1000+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)", "750+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)", "750+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)",
        30021, 0, false, List.of(new SpellData.T4cEffect(13, List.of(new SpellData.T4cEffect.EffectParam(1, "10147"), new SpellData.T4cEffect.EffectParam(2, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "(10+self.int/25+self.wis/13)")))));
  }
}
