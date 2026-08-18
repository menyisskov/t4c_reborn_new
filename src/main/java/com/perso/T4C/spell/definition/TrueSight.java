package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TrueSight {
  private TrueSight() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.true_sight}",
        "${spell.description.true_sight}",
        "80", 0, 56, 98, 69,
        false, true, "64kSpellIconEarthBoost",
        "GreenWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "300000", "0", 167246,
        null, 10262, 2, 5, 1,
        "100", "1000+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)", "750+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)", "750+if((1190-(self.level-69)*20)>=0?(1190-(self.level-69)*20):0)",
        30003, 0, false, List.of(new SpellData.T4cEffect(16, List.of(new SpellData.T4cEffect.EffectParam(1, "100"), new SpellData.T4cEffect.EffectParam(2, null))), new SpellData.T4cEffect(17, List.of(new SpellData.T4cEffect.EffectParam(1, "100"), new SpellData.T4cEffect.EffectParam(2, null)))));
  }
}
