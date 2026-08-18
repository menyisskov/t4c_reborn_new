package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Tranquility {
  private Tranquility() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.tranquility}",
        "${spell.description.tranquility}",
        "45", 0, 17, 71, 38,
        false, false, "64kSpellIconLightBoost",
        "64kSpellBless-", null, 0, 0,
        "Healing.wav", null, 0, "300000", "0", 62051,
        null, 10168, 5, 5, 1,
        "100", "1000+if((880-(self.level-38)*20)>=0?(880-(self.level-38)*20):0)", "750+if((880-(self.level-38)*20)>=0?(880-(self.level-38)*20):0)", "750+if((880-(self.level-38)*20)>=0?(880-(self.level-38)*20):0)",
        30028, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "self.true_wis/3")))));
  }
}
