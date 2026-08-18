package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobBlinkSpell {
  private MobBlinkSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_blink_spell}",
        "${spell.description.mob_blink_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirDefense",
        "Curse-",
        null,
        0,
        0,
        "Curse.wav",
        null,
        0,
        "300000",
        "15000",
        233,
        null,
        10312,
        3,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30015,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10313"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "2000")))));
  }
}
