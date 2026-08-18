package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Fireball {
  private Fireball() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.fireball}",
        "${spell.description.fireball}",
        "7", 4, 94, 16, 27,
        true, true, "64kSpellIconFireAttackArea",
        "64kSpellFireBall", "GreatExplosion-", 0, 0,
        "FireBall 2.wav", "Explosion.wav", 0, "0", "0", 34726,
        null, 10018, 1, 17, 1,
        "100", "2*(1000+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0))", "2*(750+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0))", "2*(750+if((770-(self.level-27)*20)>=0?(770-(self.level-27)*20):0))",
        30124, 30014, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d52+37+self.int/14)*self.fire/target.r_fire)"), new SpellData.T4cEffect.EffectParam(2, "-(((1d52+37+self.int/14)*self.fire/target.r_fire)*(20-r)/20)"), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
