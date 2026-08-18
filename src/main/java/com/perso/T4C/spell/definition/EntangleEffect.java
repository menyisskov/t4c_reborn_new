package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EntangleEffect {
  private EntangleEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.entangle_effect}",
        "${spell.description.entangle_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        true,
        "64kSpellIconEarthMain",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "if(self.str>500?5000:5000+1d(12501-(self.str*25)))",
        "2000",
        233,
        null,
        10151,
        2,
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
                    new SpellData.T4cEffect.EffectParam(1, null),
                    new SpellData.T4cEffect.EffectParam(2, "dodge"),
                    new SpellData.T4cEffect.EffectParam(3, "-(self.dodge*3/4)"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10349"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
