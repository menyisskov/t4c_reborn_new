package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobQuicksandsSpellEffect {
  private MobQuicksandsSpellEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_quicksands_spell_effect}",
        "${spell.description.mob_quicksands_spell_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthMain",
        "GreenWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "6000", "6000", 233,
        null, 10334, 0, 5, 1,
        "100", "0", "6000", "0",
        30003, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10247"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
