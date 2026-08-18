package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobHellChainsSpell {
  private MobHellChainsSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_hell_chains_spell}",
        "${spell.description.mob_hell_chains_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkAttackSingle",
        null, null, 0, 0,
        null, null, 0, "20000", "2000", 233,
        null, 10389, 6, 4, 1,
        "100", "2000", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10390"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
