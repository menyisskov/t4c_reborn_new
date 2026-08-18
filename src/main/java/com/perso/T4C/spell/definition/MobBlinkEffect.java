package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobBlinkEffect {
  private MobBlinkEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_blink_effect}",
        "${spell.description.mob_blink_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconAirDefense",
        null, null, 0, 0,
        null, null, 0, "10000", "0", 233,
        null, 10313, 3, 5, 1,
        "100", "2000", "0", "2000",
        0, 0, false, List.of(new SpellData.T4cEffect(15, List.of(new SpellData.T4cEffect.EffectParam(1, "100"), new SpellData.T4cEffect.EffectParam(2, "30015")))));
  }
}
