package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.HighTierSpellCurve;
import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Level-300 light group heal (T4C-0025): the high-tier successor to {@code WellspringMercy} (75),
 * sitting between the {@code LeywardBastion} (200) and {@code SanctumWard} (400) wards so the
 * light school's support line has a rung at every other tier. Same shape as Wellspring Mercy -
 * self-centered, heals the caster and grouped allies in radius - with the heal scaled to the
 * damage monsters of this level deal and gated on the shared high-tier Wisdom curve.
 */
public final class DawnwellRenewal {
  private DawnwellRenewal() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.dawnwell_renewal}",
        "${spell.description.dawnwell_renewal}",
        "600",
        5,
        HighTierSpellCurve.secondaryRequirement(300),
        HighTierSpellCurve.primaryRequirement(300),
        300,
        false,
        true,
        "64kSpellIconLightHealArea",
        "64kSpellEnergyBallWhite-",
        "HealSerious-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        5,
        "0",
        "0",
        1800000,
        null,
        99946,
        5,
        15,
        2,
        "100",
        "1700",
        "1250",
        "1250",
        30098,
        30096,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "(1d60+300+self.wis/2)*self.light/100"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "(1d60+300+self.wis/2)*self.light/100"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
