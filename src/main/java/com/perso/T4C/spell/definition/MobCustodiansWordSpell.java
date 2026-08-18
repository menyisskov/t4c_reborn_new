package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobCustodiansWordSpell {
  private MobCustodiansWordSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_custodians_word_spell}",
        "${spell.description.mob_custodians_word_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "FireWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10360,
        0,
        5,
        1,
        "100",
        "2000",
        "0",
        "0",
        30005,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "attack"),
                    new SpellData.T4cEffect.EffectParam(3, "-(self.attack/2)"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_earth"),
                    new SpellData.T4cEffect.EffectParam(3, "self.r_earth/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "fire"),
                    new SpellData.T4cEffect.EffectParam(3, "self.fire/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_air"),
                    new SpellData.T4cEffect.EffectParam(3, "self.r_air/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "water"),
                    new SpellData.T4cEffect.EffectParam(3, "self.water/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_water"),
                    new SpellData.T4cEffect.EffectParam(3, "self.r_water/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "air"),
                    new SpellData.T4cEffect.EffectParam(3, "self.air/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "r_fire"),
                    new SpellData.T4cEffect.EffectParam(3, "self.r_fire/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(3, "self.earth/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "light"),
                    new SpellData.T4cEffect.EffectParam(3, "self.light/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(3, "self.dark/3")))));
  }
}
