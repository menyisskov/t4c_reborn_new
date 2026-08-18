package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class LevelUp {
  private LevelUp() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.level_up}",
        "${spell.description.level_up}",
        "0", 0, 0, 0, 1,
        false, false, "64kSpellIconLightBoost",
        null, "LevelUp-", 0, 0,
        null, "Seraph.wav", 0, "300000", null, 0,
        null, 0, 0, 5, 2,
        "100", null, null, null,
        0, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "strength"), new SpellData.T4cEffect.EffectParam(3, "self.str/10"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "agility"), new SpellData.T4cEffect.EffectParam(3, "self.agi/10"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "endurance"), new SpellData.T4cEffect.EffectParam(3, "self.end/10"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "intelligence"), new SpellData.T4cEffect.EffectParam(3, "self.int/10"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "wisdom"), new SpellData.T4cEffect.EffectParam(3, "self.wis/10")))));
  }
}
