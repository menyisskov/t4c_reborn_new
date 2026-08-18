package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Tetrashock {
  private Tetrashock() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.tetrashock}",
        "${spell.description.tetrashock}",
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
        "300000",
        "0",
        233,
        null,
        10073,
        0,
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
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10074"),
                    new SpellData.T4cEffect.EffectParam(2, "OnAttacked"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
