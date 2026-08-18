package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class PoisonArrow {
  private PoisonArrow() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.poison_arrow}",
        "${spell.description.poison_arrow}",
        "5",
        0,
        51,
        23,
        17,
        true,
        true,
        "64kSpellIconWaterAttackSingle",
        "PoisonArrow",
        "SmallPoisonCloud-",
        0,
        0,
        "Small Projectile.wav",
        "Ice Cloud.wav",
        0,
        "501",
        "100",
        15466,
        null,
        10023,
        4,
        11,
        1,
        "100",
        "1000+if((670-(self.level-17)*20)>=0?(670-(self.level-17)*20):0)",
        "750+if((670-(self.level-17)*20)>=0?(670-(self.level-17)*20):0)",
        "750+if((670-(self.level-17)*20)>=0?(670-(self.level-17)*20):0)",
        30024,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d30+12+self.int/17)*self.water/target.r_water)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10024"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
