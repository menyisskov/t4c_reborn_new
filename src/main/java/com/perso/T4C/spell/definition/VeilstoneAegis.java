package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Originally authored under the name "Divine Veil", which turned out to collide with a real
 * t4cfantasy.com/Addon "Ancient tier" spell name; renamed to an invented name, keeping the same
 * mechanics/spellId. See {@code LeywardBastion} for this ward's higher-tier sibling.
 */
public final class VeilstoneAegis {
  private VeilstoneAegis() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.veilstone_aegis}",
        "${spell.description.veilstone_aegis}",
        "1000",
        0,
        20,
        60,
        40,
        false,
        false,
        "64kSpellIconLightDefense",
        "64kSpellBless-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        5,
        "30000",
        "0",
        80000,
        null,
        99903,
        5,
        5,
        2,
        "100",
        "1000",
        "800",
        "800",
        30028,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
