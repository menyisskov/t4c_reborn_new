package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class UltraManaSurge {
  private UltraManaSurge() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.ultra_mana_surge}",
        "${spell.description.ultra_mana_surge}",
        "39",
        0,
        375,
        90,
        150,
        false,
        true,
        "64kSpellIconNoneBoost",
        "64kSpellEnergyBallPurple-",
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
        99951,
        0,
        3,
        1,
        "100",
        "0",
        "0",
        "0",
        30055,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_earth/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "air"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_air/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "fire"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_fire/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_water/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_light/3)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(3, "2*(target.true_dark/4)")))));
  }
}
