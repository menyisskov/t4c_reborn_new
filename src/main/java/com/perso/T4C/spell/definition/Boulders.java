package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Boulders {
  private Boulders() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.boulders}",
        "${spell.description.boulders}",
        "16", 2, 55, 95, 67,
        true, true, "64kSpellIconEarthAttackArea",
        "64kSpellEnergyBallGreen-", "64kSpellBoulders-", 0, 0,
        "Healing.wav", "Boulders.wav", 0, "0", "0", 159341,
        null, 10263, 2, 17, 1,
        "100", "1000+if((1170-(self.level-67)*20)>=0?(1170-(self.level-67)*20):0)", "750+if((1170-(self.level-67)*20)>=0?(1170-(self.level-67)*20):0)", "750+if((1170-(self.level-67)*20)>=0?(1170-(self.level-67)*20):0)",
        30056, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d37+101+self.wis/9)*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(2, "-((1d37+101+self.wis/9)*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
