package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class StoneShard {
  private StoneShard() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.stone_shard}",
        "${spell.description.stone_shard}",
        "2", 0, 17, 20, 4,
        true, true, "64kSpellIconEarthAttackSingle",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "0", "0", 1328,
        null, 10035, 2, 11, 1,
        "100", "1000+if((540-(self.level-4)*20)>=0?(540-(self.level-4)*20):0)", "750+if((540-(self.level-4)*20)>=0?(540-(self.level-4)*20):0)", "750+if((540-(self.level-4)*20)>=0?(540-(self.level-4)*20):0)",
        30025, 0, true, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d9+11+self.wis/22)*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
