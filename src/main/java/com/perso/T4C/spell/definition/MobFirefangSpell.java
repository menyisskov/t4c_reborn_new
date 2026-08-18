package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobFirefangSpell {
  private MobFirefangSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_firefang_spell}",
        "${spell.description.mob_firefang_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        "FireWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "15000", "0", 233,
        null, 10355, 1, 5, 1,
        "100", "2000", "0", "0",
        30005, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10356"), new SpellData.T4cEffect.EffectParam(2, "OnAttackHit"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "250")))));
  }
}
