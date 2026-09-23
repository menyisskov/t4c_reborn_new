package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Elder tier of Avalon's ley-line wards (see {@code VeilstoneAegis}, the lower-tier version of
 * this same "full elemental resist + AC" party-ward shape) — a level-200 floor rather than
 * Veilstone Aegis's own (lower) requirements. Originally authored under the name "Sentinel",
 * which turned out to collide with a real t4cfantasy.com/Addon "Ancient tier" spell name;
 * renamed to an invented name that isn't one of the game's own real spells, keeping the same
 * mechanics/spellId. T4C-0025 moved its Intelligence/Wisdom gate onto the shared high-tier
 * curve ({@link HighTierSpellCurve}) at its own level.
 */
public final class LeywardBastion {
  private LeywardBastion() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.leyward_bastion}",
        "${spell.description.leyward_bastion}",
        "1600",
        0,
        HighTierSpellCurve.secondaryRequirement(200),
        HighTierSpellCurve.primaryRequirement(200),
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
