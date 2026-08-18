package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Shatter {
  private Shatter() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.shatter}",
        "${spell.description.shatter}",
        "5", 0, 26, 37, 18,
        true, true, "64kSpellIconEarthAttackSingle",
        "64kSpellEnergyBallGreen-", "Flak1-", 0, 0,
        "Healing.wav", "Explosion.wav", 0, "0", "0", 14292,
        null, 10037, 2, 11, 2,
        "100", "1000+if((680-(self.level-18)*20)>=0?(680-(self.level-18)*20):0)", "750+if((680-(self.level-18)*20)>=0?(680-(self.level-18)*20):0)", "750+if((680-(self.level-18)*20)>=0?(680-(self.level-18)*20):0)",
        30073, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d5+26+self.wis/17)*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
