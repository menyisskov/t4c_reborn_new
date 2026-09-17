package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Cinderburst {
  private Cinderburst() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.cinderburst}",
        "${spell.description.cinderburst}",
        "16",
        4,
        215,
        17,
        68,
        true,
        true,
        "64kSpellIconFireAttackArea",
        "64kSpellFireBall",
        "GreatExplosion-",
        0,
        0,
        "FireBall 2.wav",
        "Explosion.wav",
        0,
        "0",
        "0",
        190000,
        null,
        99912,
        1,
        17,
        1,
        "100",
        "1000+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        "750+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        "750+if((1180-(self.level-68)*20)>=0?(1180-(self.level-68)*20):0)",
        30124,
        30014,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d111+86+self.int/10)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d111+86+self.int/10)*self.fire/target.r_fire)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
