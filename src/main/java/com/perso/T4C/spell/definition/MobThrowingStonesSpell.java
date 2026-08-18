package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobThrowingStonesSpell {
  private MobThrowingStonesSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_throwing_stones_spell}",
        "${spell.description.mob_throwing_stones_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthAttackSingle",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "10000", "0", 233,
        null, 10364, 0, 4, 1,
        "100", "2000", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((self.wis-12+1d((self.wis-12)*1/2))*self.earth/target.r_earth)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "-(self.earth/4)")))));
  }
}
