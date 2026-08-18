package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobRadianceSpell {
  private MobRadianceSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_radiance_spell}",
        "${spell.description.mob_radiance_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "0",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "1000000",
        "1000000",
        233,
        null,
        10729,
        0,
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
                2,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "TRUE"),
                    new SpellData.T4cEffect.EffectParam(2, "Radiance"),
                    new SpellData.T4cEffect.EffectParam(3, "100"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10730"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "1000000")))));
  }
}
