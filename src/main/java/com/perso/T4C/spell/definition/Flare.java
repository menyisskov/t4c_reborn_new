package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Flare {
  private Flare() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.flare}",
        "${spell.description.flare}",
        "14", 0, 191, 17, 60,
        true, true, "64kSpellIconFireAttackSingle",
        "64kSpellEnergyBall-", "64kSpellFireCircle-", 0, 0,
        "Healing.wav", "Fire circle.wav", 0, "0", "0", 132824,
        null, 10128, 1, 11, 2,
        "100", "1000+if((1100-(self.level-60)*20)>=0?(1100-(self.level-60)*20):0)", "750+if((1100-(self.level-60)*20)>=0?(1100-(self.level-60)*20):0)", "750+if((1100-(self.level-60)*20)>=0?(1100-(self.level-60)*20):0)",
        30121, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d58+65+self.int/9)*self.fire/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
