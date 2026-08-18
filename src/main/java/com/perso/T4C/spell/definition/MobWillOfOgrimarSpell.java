package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobWillOfOgrimarSpell {
  private MobWillOfOgrimarSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_will_of_ogrimar_spell}",
        "${spell.description.mob_will_of_ogrimar_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkBoost",
        "BlueWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "(40000+1d20000)", "0", 233,
        null, 10368, 6, 5, 2,
        "100", "2000", "0", "0",
        30004, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "self.r_dark/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "self.r_earth/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "self.r_fire/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "self.r_air/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "self.r_water/4"))), new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-(self.maxhp/20)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
