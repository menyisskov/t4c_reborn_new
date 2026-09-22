package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Capstone of the new high-level spell tier, level 900 — a water AOE nuke sized for the top of
 * the level-1000 XP curve (Arch Drake's own level) and the toughest Avalon/endgame bosses.
 */
public final class CataclysmsHerald {
  private CataclysmsHerald() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.cataclysms_herald}",
        "${spell.description.cataclysms_herald}",
        "58",
        5,
        560,
        240,
        900,
        true,
        true,
        "64kSpellIconWaterAttackArea",
        "64kSpellEnergyBallBlue-",
        "64kSpellGlacier-",
        0,
        0,
        "Healing.wav",
        "Glacier.wav",
        0,
        "0",
        "0",
        4200000,
        null,
        99916,
        4,
        19,
        2,
        "100",
        "2100",
        "1550",
        "1550",
        30085,
        30020,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-(((1d95+250+self.int/7)*self.water/target.r_water)*18)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d95+250+self.int/7)*self.water/target.r_water)*18)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
