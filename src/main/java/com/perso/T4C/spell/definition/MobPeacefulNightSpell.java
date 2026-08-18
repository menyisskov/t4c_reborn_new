package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobPeacefulNightSpell {
  private MobPeacefulNightSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_peaceful_night_spell}",
        "${spell.description.mob_peaceful_night_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconLightDefense",
        "64kSpellEnergyBallWhite-", "64kSpellBless-", 0, 0,
        "Healing.wav", "Healing.wav", 0, "15000", "15000", 233,
        null, 10361, 5, 4, 1,
        "100", "2000", "0", "0",
        30051, 0, false, List.of(new SpellData.T4cEffect(14, List.of(new SpellData.T4cEffect.EffectParam(1, "15000"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, null), new SpellData.T4cEffect.EffectParam(4, "100"))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10247"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
