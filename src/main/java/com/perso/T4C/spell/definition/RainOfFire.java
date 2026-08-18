package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class RainOfFire {
  private RainOfFire() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.rain_of_fire}",
        "${spell.description.rain_of_fire}",
        "12",
        5,
        171,
        17,
        53,
        true,
        true,
        "64kSpellIconFireAttackArea",
        "FireBall1",
        "GreatExplosion-",
        0,
        0,
        "FireBall 2.wav",
        "Explosion.wav",
        0,
        "0",
        "0",
        103297,
        null,
        10127,
        1,
        19,
        1,
        "100",
        "2*(1000+if((1030-(self.level-53)*20)>=0?(1030-(self.level-53)*20):0))",
        "2*(750+if((1030-(self.level-53)*20)>=0?(1030-(self.level-53)*20):0))",
        "2*(750+if((1030-(self.level-53)*20)>=0?(1030-(self.level-53)*20):0))",
        30017,
        30014,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d89+68+self.int/10)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d89+68+self.int/10)*self.fire/target.r_fire)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
