package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Originally authored under the name "Omega Planetoids", which turned out to collide with a real
 * t4cfantasy.com/Addon "Ancient tier" spell name; renamed to an invented name, keeping the same
 * mechanics/spellId.
 *
 * <p>T4C-0021 balance pass: the flat "*6" multiplier left this WIS-scaling nuke (the top of the
 * "Elder" tier, level 260) doing about as much damage per mana as {@code Cinderburst} (an INT
 * nuke four tiers lower, level 68) - roughly a fifth of what an INT nuke interpolated to level
 * 260 would deal per mana. Raised to "*28" so it lands at ~93% of that interpolated INT-curve
 * figure: INT nukes keep a slight edge (elemental power/target resist/attack-type are still the
 * primary damage drivers - this only corrects the INT/WIS class gap being far wider than
 * "slight").
 */
public final class Gravebreaker {
  private Gravebreaker() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.gravebreaker}",
        "${spell.description.gravebreaker}",
        "96",
        2,
        200,
        340,
        260,
        true,
        true,
        "64kSpellIconEarthAttackArea",
        "64kSpellEnergyBallGreen-",
        "64kSpellBoulders-",
        0,
        0,
        "Healing.wav",
        "Boulders.wav",
        0,
        "0",
        "0",
        1500000,
        null,
        99905,
        2,
        17,
        1,
        "100",
        "1500",
        "1100",
        "1100",
        30056,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1, "-(((1d37+101+self.wis/9)*self.earth/target.r_earth)*28)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d37+101+self.wis/9)*self.earth/target.r_earth)*28)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
