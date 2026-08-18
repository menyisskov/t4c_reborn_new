package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class FireBolt {
  private FireBolt() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.fire_bolt}",
        "${spell.description.fire_bolt}",
        "6", 0, 80, 16, 22,
        true, true, "64kSpellIconFireAttackSingle",
        "FireBolt", "SmallExplosion-", 0, 0,
        "Small Projectile.wav", "Explosion.wav", 0, "0", "0", 24370,
        null, 10017, 1, 11, 1,
        "100", "1000+if((720-(self.level-22)*20)>=0?(720-(self.level-22)*20):0)", "750+if((720-(self.level-22)*20)>=0?(720-(self.level-22)*20):0)", "750+if((720-(self.level-22)*20)>=0?(720-(self.level-22)*20):0)",
        30014, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d45+31+self.int/16)*self.fire/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
