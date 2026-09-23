package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Originally authored under the name "Undead Annihilation", which turned out to collide with a
 * real t4cfantasy.com/Addon "Ancient tier" spell name; renamed to an invented name, keeping the
 * same mechanics/spellId.
 *
 * <p>T4C-0021 balance pass: the flat "*6" multiplier left this WIS-scaling nuke doing barely
 * more damage per mana at level 120 than {@code Cinderburst} (an INT nuke at level 68) - roughly
 * a quarter of what an INT nuke interpolated to level 120 would deal per mana. Doubled to "*12"
 * so it lands at ~90% of that interpolated INT-curve figure: INT nukes keep a slight edge (per
 * design intent - elemental power/target resist/attack-type are still the primary damage
 * drivers, this only corrects the INT/WIS class gap being far wider than "slight").
 */
public final class Sunscour {
  private Sunscour() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.sunscour}",
        "${spell.description.sunscour}",
        "30",
        0,
        100,
        260,
        120,
        true,
        true,
        "64kSpellIconLightAttackSingle",
        "64kSpellEnergyBallWhite-",
        "HealingSpell-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "0",
        "0",
        115000,
        null,
        99904,
        5,
        11,
        2,
        "100",
        "1200",
        "900",
        "900",
        30096,
        0,
        true,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(
                        1,
                        "-(((1d6+27+self.wis/16)*self.light/target.r_light)*self.wis/(20+2*self.level)*12)"),
                    new SpellData.T4cEffect.EffectParam(2, null),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
