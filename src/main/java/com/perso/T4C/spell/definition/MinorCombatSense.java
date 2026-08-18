package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MinorCombatSense {
  private MinorCombatSense() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.minor_combat_sense}",
        "${spell.description.minor_combat_sense}",
        "6",
        0,
        55,
        31,
        24,
        false,
        true,
        "64kSpellIconNoneBoost",
        "64kSpellEnergyBallPurple-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "60000",
        "0",
        28346,
        null,
        10125,
        0,
        3,
        1,
        "100",
        "1000+if((740-(self.level-24)*20)>=0?(740-(self.level-24)*20):0)",
        "750+if((740-(self.level-24)*20)>=0?(740-(self.level-24)*20):0)",
        "750+if((740-(self.level-24)*20)>=0?(740-(self.level-24)*20):0)",
        30063,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "(target.true_dodge/4)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "(target.true_attack/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 35"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_skill(35)/3")))));
  }
}
