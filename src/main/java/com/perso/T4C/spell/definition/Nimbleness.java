package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Nimbleness {
  private Nimbleness() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.nimbleness}",
        "${spell.description.nimbleness}",
        "11", 0, 60, 59, 44,
        false, true, "64kSpellIconAirBoost",
        "64kSpellBless-", null, 0, 0,
        "Healing.wav", null, 0, "60000", "0", 79343,
        null, 10172, 3, 5, 1,
        "100", "1000+if((940-(self.level-44)*20)>=0?(940-(self.level-44)*20):0)", "750+if((940-(self.level-44)*20)>=0?(940-(self.level-44)*20):0)", "750+if((940-(self.level-44)*20)>=0?(940-(self.level-44)*20):0)",
        30028, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "agility"), new SpellData.T4cEffect.EffectParam(3, "self.true_agi/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "self.true_dodge/2")))));
  }
}
