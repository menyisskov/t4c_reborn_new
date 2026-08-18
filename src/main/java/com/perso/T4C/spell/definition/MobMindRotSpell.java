package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMindRotSpell {
  private MobMindRotSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_mind_rot_spell}",
        "${spell.description.mob_mind_rot_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        "PoisonArrow", "SmallPoisonCloud-", 0, 0,
        "Small Projectile.wav", "Ice Cloud.wav", 0, "(20000+1d10000)", "0", 233,
        null, 10359, 6, 4, 1,
        "100", "2000", "0", "0",
        30024, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wis"), new SpellData.T4cEffect.EffectParam(3, "-(target.wis/2)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "int"), new SpellData.T4cEffect.EffectParam(3, "-(target.int/2)")))));
  }
}
