package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobDesolidificationSpell {
  private MobDesolidificationSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_desolidification_spell}",
        "${spell.description.mob_desolidification_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkBoost",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "(25000+1d5000)", "0", 233,
        null, 10371, 6, 5, 1,
        "100", "2000", "0", "0",
        30004, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "-(self.r_dark/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "-(self.r_earth/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "self.dodge/2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "-(self.r_air/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "-(self.r_fire/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "-(self.r_water/4)")))));
  }
}
