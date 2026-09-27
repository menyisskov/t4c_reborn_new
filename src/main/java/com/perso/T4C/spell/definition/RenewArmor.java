package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Has no formula effect of its own - casting it re-applies whichever of Barrier/Ultra Barrier,
 * Protection/Ultra Protection, Stone Skin/Ultra Stone Skin, Mana Shield/Ultra Mana Shield and Mana
 * Surge/Ultra Mana Surge the caster currently knows. That recast logic lives in {@code
 * MainGameScreen.castDefensiveSpell}, keyed off this spell's key; {@code
 * SpellRegistry.isPlayerCastable} exempts it from the "must have a T4cEffect" rule the same way
 * {@code tame_beast} is exempted, since its real effect isn't a dice formula.
 */
public final class RenewArmor {
  private RenewArmor() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.renew_armor}",
        "${spell.description.renew_armor}",
        "40",
        0,
        60,
        60,
        60,
        false,
        true,
        "64kSpellIconNoneBoost",
        "64kSpellEnergyBallBlue-",
        "BlueWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "0",
        "0",
        500000,
        null,
        99952,
        0,
        3,
        2,
        "100",
        "0",
        "0",
        "0",
        30052,
        0,
        false,
        List.of());
  }
}
