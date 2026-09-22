package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/** New high-level tier, level 480 — an air AOE nuke between {@code VoidreaveLance} (320) and
 * {@code SanctumWard} (550). Follows this fork's own "flat multiplier, no AOE falloff" nuke
 * shape (see {@code Gravebreaker}) rather than the legacy dice-only scaling. */
public final class StormcallersJudgment {
  private StormcallersJudgment() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.stormcallers_judgment}",
        "${spell.description.stormcallers_judgment}",
        "26",
        4,
        360,
        140,
        480,
        true,
        true,
        "64kSpellIconAirAttackArea",
        "64kSpellEnergyBallYellow-",
        "GreatBolt-",
        0,
        0,
        "Healing.wav",
        "Spark.wav",
        0,
        "0",
        "0",
        2400000,
        null,
        99907,
        3,
        19,
        2,
        "100",
        "1750",
        "1300",
        "1300",
        30087,
        30087,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-(((1d55+150+self.int/9)*self.air/target.r_air)*10)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d55+150+self.int/9)*self.air/target.r_air)*10)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
