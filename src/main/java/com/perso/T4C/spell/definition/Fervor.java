package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class Fervor {
  private Fervor() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.fervor}",
        "${spell.description.fervor}",
        "0",
        0,
        0,
        0,
        10,
        false,
        true,
        "64kSpellIconLightBoost",
        "64kSpellEnergyBallWhite-",
        "boulderFire-",
        0,
        0,
        "Healing.wav",
        "Healing.wav",
        0,
        "3600000",
        null,
        5805,
        null,
        10900,
        5,
        3,
        1,
        "100",
        "1000",
        "750",
        "750",
        30051,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "exp"),
                    new SpellData.T4cEffect.EffectParam(3, "900"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "unlimited"),
                    new SpellData.T4cEffect.EffectParam(3, "1")))));
  }
}
