package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TestSanctuaryIsland {
  private TestSanctuaryIsland() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.test_sanctuary_island}",
        "${spell.description.test_sanctuary_island}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconAirMain",
        "64kSpellEnergyBallBlue-", "Freeze-", 0, 0,
        "Healing.wav", "Freeze.wav", 0, "0", "0", 233,
        null, 10297, 0, 0, 1,
        "100", "0", "0", "0",
        30080, 0, false, List.of(new SpellData.T4cEffect(3, List.of(new SpellData.T4cEffect.EffectParam(1, "20020"), new SpellData.T4cEffect.EffectParam(2, "2884352000")))));
  }
}
