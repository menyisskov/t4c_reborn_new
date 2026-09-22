package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * First rung of a new high-level tier (levels 320-900) filling the gap above the existing
 * "Elder" ley-line spells (level 40-260) and the level-1000 curve cap / Avalon boss band
 * (550-650) — a single-target dark nuke for characters starting to face those tougher monsters.
 */
public final class VoidreaveLance {
  private VoidreaveLance() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.voidreave_lance}",
        "${spell.description.voidreave_lance}",
        "18",
        0,
        260,
        90,
        320,
        true,
        true,
        "64kSpellIconDarkAttackSingle",
        "64kSpellEnergyBallBlack-",
        "Curse-",
        0,
        0,
        "Healing.wav",
        "Curse.wav",
        0,
        "0",
        "0",
        1850000,
        null,
        99906,
        6,
        11,
        1,
        "100",
        "1600",
        "1200",
        "1200",
        30062,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-(((1d42+112+self.int/10)*self.dark/target.r_dark)*7)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
