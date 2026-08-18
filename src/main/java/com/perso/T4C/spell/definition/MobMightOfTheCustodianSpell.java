package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMightOfTheCustodianSpell {
  private MobMightOfTheCustodianSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_might_of_the_custodian_spell}",
        "${spell.description.mob_might_of_the_custodian_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        "FireWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "(15000+1d5000)", "0", 233,
        null, 10363, 1, 5, 1,
        "100", "2000", "0", "0",
        30005, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "self.ac/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "self.attack")))));
  }
}
