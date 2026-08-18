package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MajorCombatSense {
  private MajorCombatSense() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.major_combat_sense}",
        "${spell.description.major_combat_sense}",
        "15", 0, 114, 64, 66,
        false, true, "64kSpellIconNoneBoost",
        "64kSpellEnergyBallPurple-", "Curse-", 0, 0,
        "Healing.wav", "Curse.wav", 0, "30000", "0", 155442,
        null, 10144, 0, 3, 1,
        "100", "1000+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)", "750+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)", "750+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)",
        30063, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "target.true_dodge/3"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "attack"), new SpellData.T4cEffect.EffectParam(3, "target.true_attack/2"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "STR"), new SpellData.T4cEffect.EffectParam(3, "target.true_str/4"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "skill 35"), new SpellData.T4cEffect.EffectParam(3, "target.true_skill(35)/2")))));
  }
}
