package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Canon "Ancient tier" (t4cfantasy.com/Addon, level 200+ group support spells) group defensive
 * ward — sits alongside Divine Veil, Clemancy, Army's Paeon, Mage's Ballad, Wanderer's Minuet
 * and Sentinel itself in that tier. Modeled directly on the already-shipped Divine Veil (same
 * "full elemental resist + AC" party-ward shape), scaled up in cost/level to this tier's actual
 * level-200 floor rather than Divine Veil's own (lower) requirements.
 */
public final class Sentinel {
  private Sentinel() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.sentinel}",
        "${spell.description.sentinel}",
        "1600",
        0,
        30,
        90,
        200,
        false,
        true,
        "64kSpellIconLightDefense",
        "64kSpellBless-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        5,
        "45000",
        "0",
        220000,
        null,
        99914,
        5,
        5,
        2,
        "100",
        "1400",
        "1000",
        "1000",
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
