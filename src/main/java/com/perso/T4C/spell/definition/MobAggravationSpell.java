package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobAggravationSpell {
  private MobAggravationSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_aggravation_spell}",
        "${spell.description.mob_aggravation_spell}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconNoneAttackSingle",
        "Lightning", "ElectricShield-", 0, 0,
        "Lightning.wav", "Electric Shield.wav", 0, "(30000+1d10000)", "0", 233,
        null, 10375, 0, 4, 1,
        "100", "2000", "0", "0",
        30002, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "-(target.r_dark/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "-(target.r_earth/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "-(target.r_fire/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "-(target.r_air/4)"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "-(target.r_water/4)")))));
  }
}
