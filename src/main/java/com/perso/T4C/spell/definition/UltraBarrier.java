package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UltraBarrier {
  private UltraBarrier() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ultra_barrier}",
        "${spell.description.ultra_barrier}",
        "10",
        0,
        375,
        90,
        150,
        false,
        true,
        "64kSpellIconWaterDefense",
        "64kSpellEnergyBallBlue-",
        "BlueWipe-",
        0,
        0,
        "Healing.wav",
        "Mind Shield.wav",
        0,
        "1200000",
        "0",
        1000000,
        null,
        99947,
        4,
        3,
        1,
        "100",
        "0",
        "0",
        "0",
        30052,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "AC"),
                    new SpellData.T4cEffect.EffectParam(
                        3, "(2*(5+self.int/50+self.wis/25))")))));
  }
}
