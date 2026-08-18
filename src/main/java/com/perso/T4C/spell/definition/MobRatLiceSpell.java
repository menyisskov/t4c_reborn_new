package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobRatLiceSpell {
  private MobRatLiceSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_rat_lice_spell}",
        "${spell.description.mob_rat_lice_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconNoneMain",
        "PoisonArrow", "SmallPoisonCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0, "20000", "0", 233,
        null, 10351, 0, 4, 1,
        "100", "2000", "0", "0",
        30024, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "-(target.dodge/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "-(target.attack/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "-target.true_skill(35)/4")))));
  }
}
