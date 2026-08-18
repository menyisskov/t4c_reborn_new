package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class StoneSkin {
  private StoneSkin() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.stone_skin}",
        "${spell.description.stone_skin}",
        "45", 0, 37, 59, 37,
        false, false, "64kSpellIconEarthDefense",
        "RockyFly-", null, 0, 0,
        "Rocks Fly.wav", null, 0, "300000", "0", 59326,
        null, 10147, 2, 5, 1,
        "100", "1000+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)", "750+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)", "750+if((870-(self.level-37)*20)>=0?(870-(self.level-37)*20):0)",
        30021, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "(10+self.int/25+self.wis/13)")))));
  }
}
