package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemHammerOfTrueSight {
  private ItemHammerOfTrueSight() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_hammer_of_true_sight}",
        "${spell.description.item_hammer_of_true_sight}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
        "GreenWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "300000",
        "0",
        233,
        null,
        10677,
        2,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30003,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10262"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                13,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10399"),
                    new SpellData.T4cEffect.EffectParam(2, "100"))),
            new SpellData.T4cEffect(
                16,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30020"))),
            new SpellData.T4cEffect(
                17,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30003")))));
  }
}
