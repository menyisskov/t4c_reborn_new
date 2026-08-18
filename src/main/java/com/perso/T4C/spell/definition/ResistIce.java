package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ResistIce {
  private ResistIce() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.resist_ice}",
        "${spell.description.resist_ice}",
        "11", 0, 150, 16, 46,
        false, true, "64kSpellIconFireDefense",
        "FireWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "60000", "0", 85456,
        null, 10170, 1, 5, 1,
        "100", "1000+if((960-(self.level-46)*20)>=0?(960-(self.level-46)*20):0)", "750+if((960-(self.level-46)*20)>=0?(960-(self.level-46)*20):0)", "750+if((960-(self.level-46)*20)>=0?(960-(self.level-46)*20):0)",
        30005, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "self.true_r_water")))));
  }
}
