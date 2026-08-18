package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Entangle {
  private Entangle() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.entangle}",
        "${spell.description.entangle}",
        "10", 0, 34, 52, 31,
        false, true, "64kSpellIconEarthMain",
        "64kSpellEnergyBallGreen-", "64kSpellEntangle-", 0, 0,
        "Healing.wav", "Entangle.wav", 0, "0", "0", 43972,
        null, 10150, 2, 9, 1,
        "100", "1000+if((810-(self.level-31)*20)>=0?(810-(self.level-31)*20):0)", "750+if((810-(self.level-31)*20)>=0?(810-(self.level-31)*20):0)", "750+if((810-(self.level-31)*20)>=0?(810-(self.level-31)*20):0)",
        30068, 0, true, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10151"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "50*self.earth/target.r_earth"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
