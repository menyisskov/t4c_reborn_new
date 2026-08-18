package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WrathOfDrake {
  private WrathOfDrake() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wrath_of_drake}",
        "${spell.description.wrath_of_drake}",
        "0", 10, 0, 0, 0,
        true, false, "64kSpellIconFireAttackSingle",
        "64kSpellFireBall", "GreatExplosion-", 0, 0,
        "FireBall 2.wav", "Explosion.wav", 0, "0", "0", 233,
        null, 10085, 1, 8, 2,
        "100", "200", "0", "0",
        30124, 30014, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1000*self.int/self.wis)*100/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
