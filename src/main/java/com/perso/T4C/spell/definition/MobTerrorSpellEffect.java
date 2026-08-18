package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobTerrorSpellEffect {
  private MobTerrorSpellEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_terror_spell_effect}",
        "${spell.description.mob_terror_spell_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainSingle",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "15000",
        "2000",
        233,
        null,
        10318,
        6,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        0,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10319"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "0")))));
  }
}
