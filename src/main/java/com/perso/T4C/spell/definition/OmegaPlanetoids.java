package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class OmegaPlanetoids {
  private OmegaPlanetoids() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.omega_planetoids}",
        "${spell.description.omega_planetoids}",
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
                        1, "-(((1d37+101+self.wis/9)*self.earth/target.r_earth)*6)"),
                    new SpellData.T4cEffect.EffectParam(
                        2, "-(((1d37+101+self.wis/9)*self.earth/target.r_earth)*6)"),
                    new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
