package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TestMarkedForDeath {
  private TestMarkedForDeath() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.test_marked_for_death}",
        "${spell.description.test_marked_for_death}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainArea",
        null, null, 0, 0,
        null, null, 0, "180000", "0", 233,
        null, 10446, 6, 4, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10447"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "180000")))));
  }
}
