package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Inferno {
  private Inferno() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.inferno}",
        "${spell.description.inferno}",
        "19", 0, 259, 17, 83,
        true, true, "64kSpellIconFireAttackSingle",
        "64kSpellEnergyBall-", "64kSpellFireCircle-", 0, 0,
        "Healing.wav", "Fire circle.wav", 0, "0", "0", 226492,
        null, 10130, 1, 11, 2,
        "100", "1000+if((1330-(self.level-83)*20)>=0?(1330-(self.level-83)*20):0)", "750+if((1330-(self.level-83)*20)>=0?(1330-(self.level-83)*20):0)", "750+if((1330-(self.level-83)*20)>=0?(1330-(self.level-83)*20):0)",
        30121, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d76+89+self.int/8)*self.fire/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
