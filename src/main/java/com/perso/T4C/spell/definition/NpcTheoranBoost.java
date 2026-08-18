package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class NpcTheoranBoost {
  private NpcTheoranBoost() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.npc_theoran_boost}",
        "${spell.description.npc_theoran_boost}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconLightBoost",
        "64kSpellBless-", null, 0, 0,
        "Healing.wav", null, 0, "150000", "0", 233,
        null, 10692, 5, 4, 1,
        "100", "0", "0", "0",
        30028, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_air/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_earth/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_water/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_dark/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_fire/5")))));
  }
}
