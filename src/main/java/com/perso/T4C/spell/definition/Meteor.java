package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Meteor {
  private Meteor() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.meteor}",
        "${spell.description.meteor}",
        "23",
        2,
        309,
        18,
        100,
        true,
        true,
        "64kSpellIconFireAttackSingle",
        "64kSpellEnergyBall-",
        "64kSpellMeteor-",
        0,
        0,
        "Healing.wav",
        "Meteor.wav",
        0,
        "0",
        "0",
        307129,
        null,
        10126,
        1,
        19,
        1,
        "100",
        "1000+if((1500-(self.level-100)*20)>=0?(1500-(self.level-100)*20):0)",
        "750+if((1500-(self.level-100)*20)>=0?(1500-(self.level-100)*20):0)",
        "750+if((1500-(self.level-100)*20)>=0?(1500-(self.level-100)*20):0)",
        30102,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d157+125+self.int/7)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-((1d157+125+self.int/7)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
