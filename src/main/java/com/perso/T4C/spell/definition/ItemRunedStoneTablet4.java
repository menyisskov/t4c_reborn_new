package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemRunedStoneTablet4 {
  private ItemRunedStoneTablet4() {}

  public static SpellData definition() {
    return new SpellData(
        "${item.runed_stone_tablet}",
        "",
        "0", 0, 0, 0, 0,
        false, false, "",
        null, null, 0, 0,
        null, null, 0, "0", "0", 0,
        null, 10756, 0, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(3, List.of(new SpellData.T4cEffect.EffectParam(1, "30509"), new SpellData.T4cEffect.EffectParam(2, "1"))), new SpellData.T4cEffect(7, List.of(new SpellData.T4cEffect.EffectParam(1, "681+1d4"), new SpellData.T4cEffect.EffectParam(2, "827+1d8"), new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
