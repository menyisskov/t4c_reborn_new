package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobColosseumUpgradeSpell4 {
  private MobColosseumUpgradeSpell4() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_colosseum_upgrade_spell_4}",
        "${spell.description.mob_colosseum_upgrade_spell_4}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconAirDefense",
        null, null, 0, 0,
        null, null, 0, "120000", "0", 233,
        null, 10716, 0, 5, 1,
        "if(self.viewflag(30429)=1?100:0)", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_dark"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_dark/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_earth"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_earth/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_fire"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_fire/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_air"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_air/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "r_water"), new SpellData.T4cEffect.EffectParam(3, "target.true_r_water/3"))), new SpellData.T4cEffect(3, List.of(new SpellData.T4cEffect.EffectParam(1, "30429"), new SpellData.T4cEffect.EffectParam(2, "0")))));
  }
}
