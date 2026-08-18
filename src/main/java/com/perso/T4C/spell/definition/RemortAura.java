package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class RemortAura {
  private RemortAura() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.remort_aura}",
        "${spell.description.remort_aura}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneDefense",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "infinite",
        "0",
        233,
        null,
        10696,
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
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10699"),
                    new SpellData.T4cEffect.EffectParam(2, "OnHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "200"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10698"),
                    new SpellData.T4cEffect.EffectParam(2, "OnAttackHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "200"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10697"),
                    new SpellData.T4cEffect.EffectParam(2, "OnHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "200")))));
  }
}
