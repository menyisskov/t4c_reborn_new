package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobColosseumUpgradeSpell2 {
  private MobColosseumUpgradeSpell2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_colosseum_upgrade_spell_2}",
        "${spell.description.mob_colosseum_upgrade_spell_2}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconEarthDefense",
        null, null, 0, 0,
        null, null, 0, "120000", "0", 233,
        null, 10714, 0, 5, 1,
        "if(self.viewflag(30427)=1?100:0)", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "AC"), new SpellData.T4cEffect.EffectParam(3, "self.ac/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "(target.true_dodge/3)"))), new SpellData.T4cEffect(3, List.of(new SpellData.T4cEffect.EffectParam(1, "30427"), new SpellData.T4cEffect.EffectParam(2, "0")))));
  }
}
