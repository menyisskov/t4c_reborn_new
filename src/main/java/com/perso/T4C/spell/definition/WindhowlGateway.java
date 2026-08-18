package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WindhowlGateway {
  private WindhowlGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.windhowl_gateway}",
        "${spell.description.windhowl_gateway}",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 0,
        null, 10098, 0, 5, 1,
        "100", "0", "0", "0",
        30012, 0, false, List.of(new SpellData.T4cEffect(7, List.of(new SpellData.T4cEffect.EffectParam(1, "1812"), new SpellData.T4cEffect.EffectParam(2, "1293"), new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
