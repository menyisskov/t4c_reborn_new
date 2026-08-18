package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class LighthavenImprovedGateway {
  private LighthavenImprovedGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.lighthaven_improved_gateway}",
        "${spell.description.lighthaven_improved_gateway}",
        "self.maxmana", 10, 0, 0, 0,
        false, false, "64kSpellIconAirMain",
        null, "Lightning", 0, 0,
        null, "LightningBolt", 0, "19000", "1000", 233,
        null, 10275, 3, 15, 1,
        "100", "60000", "20000", "60000",
        0, 30002, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10276"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10240"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "20000")))));
  }
}
