package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class FireDart {
  private FireDart() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.fire_dart}",
        "${spell.description.fire_dart}",
        "1",
        0,
        21,
        15,
        2,
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
        532,
        null,
        10009,
        1,
        11,
        1,
        "100",
        "1000+if((520-(self.level-2)*20)>=0?(520-(self.level-2)*20):0)",
        "750+if((520-(self.level-2)*20)>=0?(520-(self.level-2)*20):0)",
        "750+if((520-(self.level-2)*20)>=0?(520-(self.level-2)*20):0)",
        30014,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-((1d17+6+self.int/23)*self.fire/target.r_fire)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
