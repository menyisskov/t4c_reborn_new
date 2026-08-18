package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class WrathOfTheAncients {
  private WrathOfTheAncients() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.wrath_of_the_ancients}",
        "${spell.description.wrath_of_the_ancients}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        null, null, 0, 0,
        null, null, 0, "infinite", "0", 233,
        null, 10815, 0, 0, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10816"), new SpellData.T4cEffect.EffectParam(2, "OnHit"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
