package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * New high-level tier, level 550 — the party-ward successor to {@code LeywardBastion} (200) and
 * {@code VeilstoneAegis} (40): same 6-way resist/AC shape, scaled up (250/150 instead of 100),
 * plus a max-hp buff so the extra tier is worth its much higher mana cost.
 */
public final class SanctumWard {
  private SanctumWard() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.sanctum_ward}",
        "${spell.description.sanctum_ward}",
        "2200",
        0,
        50,
        480,
        550,
        false,
        false,
        "64kSpellIconLightDefense",
        "64kSpellBless-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        6,
        "60000",
        "0",
        2600000,
        null,
        99908,
        5,
        5,
        2,
        "100",
        "1700",
        "1250",
        "1250",
        30028,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "250"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "250"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "250"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "250"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "250"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(3, "150"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "max hp"),
                    new SpellData.T4cEffect.EffectParam(3, "self.wis*2")))));
  }
}
