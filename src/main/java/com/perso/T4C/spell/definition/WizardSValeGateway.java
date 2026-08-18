package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WizardSValeGateway {
  private WizardSValeGateway() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wizard_s_vale_gateway}",
        "${spell.description.wizard_s_vale_gateway}",
        "self.maxmana", 0, 86, 84, 70,
        false, false, "64kSpellIconAirDefense",
        null, null, 0, 0,
        null, null, 0, "9000", "1000", 171252,
        null, 10255, 3, 5, 1,
        "100", "30000", "10000", "30000",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10238"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null))), new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10244"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "10000")))));
  }
}
