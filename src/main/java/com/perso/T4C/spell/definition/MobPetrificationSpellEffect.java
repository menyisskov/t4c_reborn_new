package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobPetrificationSpellEffect {
  private MobPetrificationSpellEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_petrification_spell_effect}",
        "${spell.description.mob_petrification_spell_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        null, null, 0, 0,
        null, null, 0, "12000", "2000", 233,
        null, 10315, 6, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10316"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "0"))), new SpellData.T4cEffect(14, List.of(new SpellData.T4cEffect.EffectParam(1, "12000"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "12000"), new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
