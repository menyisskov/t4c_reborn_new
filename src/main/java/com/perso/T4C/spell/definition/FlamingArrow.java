package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class FlamingArrow {
  private FlamingArrow() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.flaming_arrow}",
        "${spell.description.flaming_arrow}",
        "3",
        0,
        44,
        15,
        10,
        true,
        true,
        "64kSpellIconFireAttackSingle",
        "FireBolt",
        "SmallExplosion-",
        0,
        0,
        "Small Projectile.wav",
        "Explosion.wav",
        0,
        "0",
        "0",
        20372,
        null,
        10016,
        1,
        11,
        1,
        "100",
        "1000+if((600-(self.level-10)*20)>=0?(600-(self.level-10)*20):0)",
        "750+if((600-(self.level-10)*20)>=0?(600-(self.level-10)*20):0)",
        "750+if((600-(self.level-10)*20)>=0?(600-(self.level-10)*20):0)",
        30014,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d28+16+self.int/19)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
