package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

/**
 * Originally authored under the name "Clemancy", which turned out to collide with a real
 * t4cfantasy.com/Addon "Ancient tier" spell name; renamed to an invented name, keeping the same
 * mechanics/spellId.
 */
public final class WellspringMercy {
  private WellspringMercy() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wellspring_mercy}",
        "${spell.description.wellspring_mercy}",
        "1000",
        5,
        30,
        150,
        75,
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
        250000,
        null,
        99902,
        5,
        15,
        2,
        "100",
        "1200",
        "900",
        "900",
        30098,
        30096,
        false,
        List.of(
            new SpellData.T4cEffect(
                1,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "(1d14+247+self.wis/8)*self.light/100"),
                    new SpellData.T4cEffect.EffectParam(2, "(1d14+247+self.wis/8)*self.light/100"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
