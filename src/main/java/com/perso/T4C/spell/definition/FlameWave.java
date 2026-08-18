package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class FlameWave {
  private FlameWave() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.flame_wave}",
        "${spell.description.flame_wave}",
        "10",
        5,
        133,
        16,
        40,
        true,
        false,
        "64kSpellIconFireAttackArea",
        "64kSpellEnergyBall-",
        "SemiBigExplosion-",
        0,
        0,
        "Healing.wav",
        "SemiBig Explosion.wav",
        0,
        "0",
        "0",
        67637,
        null,
        10076,
        1,
        18,
        1,
        "100",
        "2*(1000+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0))",
        "2*(750+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0))",
        "2*(750+if((900-(self.level-40)*20)>=0?(900-(self.level-40)*20):0))",
        30114,
        30124,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "0"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d71+52+self.int/12)*self.fire/target.r_fire)*(20-r)/20)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
