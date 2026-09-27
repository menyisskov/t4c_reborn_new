package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UltraManaShield {
  private UltraManaShield() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ultra_mana_shield}",
        "${spell.description.ultra_mana_shield}",
        "14",
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
        99950,
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
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_dark"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_r_dark/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_r_fire/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_r_earth/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_r_water/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_r_air/3)")))));
  }
}
