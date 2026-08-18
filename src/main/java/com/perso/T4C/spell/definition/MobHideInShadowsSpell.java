package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobHideInShadowsSpell {
  private MobHideInShadowsSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_hide_in_shadows_spell}",
        "${spell.description.mob_hide_in_shadows_spell}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconAirBoost",
        "Flak2-",
        null,
        0,
        0,
        "Explosion.wav",
        null,
        0,
        "(self.dark*500)",
        "0",
        233,
        null,
        10366,
        0,
        5,
        1,
        "100",
        "10000",
        "10000",
        "10000",
        30011,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                15,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "100"),
                    new SpellData.T4cEffect.EffectParam(2, "30015")))));
  }
}
