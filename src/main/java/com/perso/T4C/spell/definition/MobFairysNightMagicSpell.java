package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobFairysNightMagicSpell {
  private MobFairysNightMagicSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_fairys_night_magic_spell}",
        "${spell.description.mob_fairys_night_magic_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconEarthBoost",
        "RockyFly-",
        null,
        0,
        0,
        "Rocks Fly.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10365,
        2,
        5,
        1,
        "100",
        "2000",
        "0",
        "0",
        30021,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dark"),
                    new SpellData.T4cEffect.EffectParam(3, "self.dark/3"))),
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "earth"),
                    new SpellData.T4cEffect.EffectParam(3, "self.earth/3")))));
  }
}
