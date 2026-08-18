package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfMajorCombatSense {
  private ItemScrollOfMajorCombatSense() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_major_combat_sense}",
        "${spell.description.item_scroll_of_major_combat_sense}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneBoost",
        "Curse-",
        null,
        0,
        0,
        "Curse.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10661,
        0,
        5,
        1,
        "100",
        "1000+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)",
        "750+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)",
        "750+if((1160-(self.level-66)*20)>=0?(1160-(self.level-66)*20):0)",
        30015,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10144"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_dodge/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "STR"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_str/4"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_attack/2"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "skill 35"),
                    new SpellData.T4cEffect.EffectParam(3, "target.true_skill(35)/2")))));
  }
}
