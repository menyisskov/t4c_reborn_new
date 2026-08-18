package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemGreatAxeOfTheCrowEffect {
  private ItemGreatAxeOfTheCrowEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_great_axe_of_the_crow_effect}",
        "${spell.description.item_great_axe_of_the_crow_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconDarkDrainSingle",
        "GreenWipe-", null, 0, 0,
        "Mind Shield.wav", null, 0, "120000", "0", 233,
        null, 10440, 6, 5, 2,
        "100", "0", "0", "0",
        30003, 0, false, List.of(new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "ac"), new SpellData.T4cEffect.EffectParam(3, "-target.ac/5"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "dodge"), new SpellData.T4cEffect.EffectParam(3, "-target.dodge/4"))), new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "-((1d39+31)*self.dark/target.r_dark)"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100")))));
  }
}
