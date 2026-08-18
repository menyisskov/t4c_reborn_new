package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemMantleOfDeathEffect {
  private ItemMantleOfDeathEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_mantle_of_death_effect}",
        "${spell.description.item_mantle_of_death_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        null, null, 0, 0,
        null, null, 0, "20000", "2500", 233,
        null, 10309, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10310"), new SpellData.T4cEffect.EffectParam(2, "OnTimer"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, null)))));
  }
}
