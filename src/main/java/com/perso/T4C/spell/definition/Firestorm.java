package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Firestorm {
  private Firestorm() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.firestorm}",
        "${spell.description.firestorm}",
        "18", 6, 238, 17, 76,
        true, true, "64kSpellIconFireAttackArea",
        "64kSpellEnergyBall-", "64kSpellFireCircle-", 0, 0,
        "Healing.wav", "Fire circle.wav", 0, "0", "0", 196028,
        null, 10129, 1, 19, 1,
        "100", "2*(1000+if((1260-(self.level-76)*20)>=0?(1260-(self.level-76)*20):0))", "2*(750+if((1260-(self.level-76)*20)>=0?(1260-(self.level-76)*20):0))", "2*(750+if((1260-(self.level-76)*20)>=0?(1260-(self.level-76)*20):0))",
        30121, 30121, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d122+96+self.int/8)*self.fire/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d122+96+self.int/8)*self.fire/target.r_fire)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
