package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WrathOfMarc {
  private WrathOfMarc() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wrath_of_marc}",
        "${spell.description.wrath_of_marc}",
        "0",
        20,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthDefense",
        "64kSpellBoulders-",
        null,
        0,
        0,
        "Boulders.wav",
        null,
        0,
        "120000",
        "0",
        233,
        null,
        10400,
        2,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30031,
        30031,
        false,
        List.of(
            new SpellData.T4cEffect(
                14,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "120000"),
                    new SpellData.T4cEffect.EffectParam(2, "120000"),
                    new SpellData.T4cEffect.EffectParam(3, "120000"),
                    new SpellData.T4cEffect.EffectParam(
                        4, "if(target.viewflag(30400)>0?0:100)")))));
  }
}
