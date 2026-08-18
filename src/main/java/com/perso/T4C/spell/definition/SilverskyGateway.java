package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class SilverskyGateway {
  private SilverskyGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.silversky_gateway}",
        "${spell.description.silversky_gateway}",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 0,
        null, 10121, 0, 5, 1,
        "100", "0", "0", "0",
        30012, 0, false, List.of(new SpellData.T4cEffect(7, List.of(new SpellData.T4cEffect.EffectParam(1, "1495"), new SpellData.T4cEffect.EffectParam(2, "2470"), new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
