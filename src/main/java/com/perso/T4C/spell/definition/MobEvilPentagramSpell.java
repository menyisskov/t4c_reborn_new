package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobEvilPentagramSpell {
  private MobEvilPentagramSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_evil_pentagram_spell}",
        "${spell.description.mob_evil_pentagram_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkBoost",
        "Pentacle-", null, 0, 0,
        "Vampire Dying.wav", null, 0, "30000", "0", 233,
        null, 10367, 6, 5, 2,
        "100", "2000", "0", "0",
        30006, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "self.maxhp"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "int"), new SpellData.T4cEffect.EffectParam(3, "self.int/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "self.attack/3")))));
  }
}
