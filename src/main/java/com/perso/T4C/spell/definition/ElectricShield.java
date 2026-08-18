package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ElectricShield {
  private ElectricShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.electric_shield}",
        "${spell.description.electric_shield}",
        "24", 0, 65, 64, 49,
        false, true, "64kSpellIconAirBoost",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "120000", "0", 94941,
        null, 10081, 3, 5, 1,
        "100", "1000+if((990-(self.level-49)*20)>=0?(990-(self.level-49)*20):0)", "750+if((990-(self.level-49)*20)>=0?(990-(self.level-49)*20):0)", "750+if((990-(self.level-49)*20)>=0?(990-(self.level-49)*20):0)",
        30004, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10083"), new SpellData.T4cEffect.EffectParam(2, "OnHit"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
