package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class LighthavenGateway {
  private LighthavenGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.lighthaven_gateway}",
        "${spell.description.lighthaven_gateway}",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 0,
        null, 10019, 0, 5, 1,
        "100", "0", "0", "0",
        30012, 0, false, List.of(new SpellData.T4cEffect(7, List.of(new SpellData.T4cEffect.EffectParam(1, "2941"), new SpellData.T4cEffect.EffectParam(2, "1062"), new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
