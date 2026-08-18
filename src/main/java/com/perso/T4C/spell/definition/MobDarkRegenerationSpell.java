package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobDarkRegenerationSpell {
  private MobDarkRegenerationSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_dark_regeneration_spell}",
        "${spell.description.mob_dark_regeneration_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkBoost",
        "BlueWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "30000",
        "1000",
        233,
        null,
        10369,
        6,
        5,
        1,
        "100",
        "2000",
        "0",
        "0",
        30004,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-(self.dodge/2)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10370"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
