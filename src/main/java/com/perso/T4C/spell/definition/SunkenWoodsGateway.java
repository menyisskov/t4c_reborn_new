package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class SunkenWoodsGateway {
  private SunkenWoodsGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.sunken_woods_gateway}",
        "${spell.description.sunken_woods_gateway}",
        "self.maxmana",
        0,
        86,
        84,
        70,
        false,
        false,
        "64kSpellIconAirDefense",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "9000",
        "1000",
        171252,
        null,
        10250,
        3,
        5,
        1,
        "100",
        "30000",
        "10000",
        "30000",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10239"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "10000"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10238"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "0")))));
  }
}
