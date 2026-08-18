package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBoneswordOfTheBerserkerEffect {
  private ItemBoneswordOfTheBerserkerEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_bonesword_of_the_berserker_effect}",
        "${spell.description.item_bonesword_of_the_berserker_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "64kSpellFireCircle-",
        null,
        0,
        0,
        "Fire circle.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10690,
        1,
        5,
        1,
        "100",
        "2000",
        "0",
        "0",
        30120,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10691"),
                    new SpellData.T4cEffect.EffectParam(2, "OnAttackHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-self.true_dodge/5"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_attack/5"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "ac"),
                    new SpellData.T4cEffect.EffectParam(3, "-self.ac/5"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "str"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_str/5")))));
  }
}
