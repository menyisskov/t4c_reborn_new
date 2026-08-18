package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobVaporizeSpell {
  private MobVaporizeSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_vaporize_spell}",
        "${spell.description.mob_vaporize_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconLightMain",
        "StoneShard", "RockyFly-", 0, 0,
        "Small Projectile.wav", "Rocks Fly.wav", 0, "0", "0", 233,
        null, 10450, 0, 0, 1,
        "100", "0", "0", "0",
        30025, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10451"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "5000")))));
  }
}
