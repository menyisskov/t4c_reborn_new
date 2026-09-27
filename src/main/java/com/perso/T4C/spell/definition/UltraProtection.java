package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UltraProtection {
  private UltraProtection() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ultra_protection}",
        "${spell.description.ultra_protection}",
        "9",
        0,
        90,
        375,
        150,
        false,
        true,
        "64kSpellIconEarthDefense",
        "64kSpellEnergyBallGreen-",
        "GreenWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "1200000",
        "0",
        1000000,
        null,
        99948,
        2,
        3,
        1,
        "100",
        "0",
        "0",
        "0",
        30092,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "(2*(3+self.int/100+self.wis/50))")))));
  }
}
