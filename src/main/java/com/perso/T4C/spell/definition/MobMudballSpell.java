package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMudballSpell {
  private MobMudballSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_mudball_spell}",
        "${spell.description.mob_mudball_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthAttackSingle",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "(10000+1d5000)", "0", 233,
        null, 10348, 2, 4, 1,
        "100", "2000", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((self.wis-12+1d((self.wis-12)*1/2))*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "-target.attack/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "-target.dodge/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "-target.true_skill(35)/5")))));
  }
}
