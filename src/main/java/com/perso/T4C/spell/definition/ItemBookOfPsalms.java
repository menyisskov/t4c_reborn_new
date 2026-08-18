package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemBookOfPsalms {
  private ItemBookOfPsalms() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_book_of_psalms}",
        "${spell.description.item_book_of_psalms}",
        "0", 0, 0, 0, 0,
        false, false, "64kIconScroll",
        "HealingSpell-", null, 0, 0,
        "Healing.wav", null, 0, "0", "0", 233,
        null, 10482, 5, 5, 1,
        "100", "0", "0", "0",
        30001, 0, false, List.of(new SpellData.T4cEffect(1, List.of(new SpellData.T4cEffect.EffectParam(1, "50"), new SpellData.T4cEffect.EffectParam(2, null), new SpellData.T4cEffect.EffectParam(3, "100"))), new SpellData.T4cEffect(2, List.of(new SpellData.T4cEffect.EffectParam(1, null), new SpellData.T4cEffect.EffectParam(2, "light"), new SpellData.T4cEffect.EffectParam(3, "target.true_light/3")))));
  }
}
