package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemSapphireHiltedSpellEffect {
  private ItemSapphireHiltedSpellEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_sapphire_hilted_spell_effect}",
        "${spell.description.item_sapphire_hilted_spell_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10411,
        3,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "agility"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_agi/4"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "strength"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_str/4"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "endurance"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_end/4"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_dodge/4"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "self.true_attack/4")))));
  }
}
