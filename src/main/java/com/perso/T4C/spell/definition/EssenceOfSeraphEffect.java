package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EssenceOfSeraphEffect {
  private EssenceOfSeraphEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.essence_of_seraph_effect}",
        "${spell.description.essence_of_seraph_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirDefense",
        null,
        "64kSpellBless-",
        0,
        0,
        null,
        "Healing",
        0,
        "15000",
        "2500",
        233,
        null,
        10804,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        0,
        30028,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "true"),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "self.ac/4"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10805"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "0")))));
  }
}
