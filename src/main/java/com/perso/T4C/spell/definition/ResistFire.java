package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ResistFire {
  private ResistFire() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.resist_fire}",
        "${spell.description.resist_fire}",
        "10", 0, 105, 34, 43,
        false, true, "64kSpellIconWaterDefense",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "60000", "0", 76350,
        null, 10154, 4, 5, 1,
        "100", "1000+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)", "750+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)", "750+if((930-(self.level-43)*20)>=0?(930-(self.level-43)*20):0)",
        30004, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "self.true_r_fire")))));
  }
}
