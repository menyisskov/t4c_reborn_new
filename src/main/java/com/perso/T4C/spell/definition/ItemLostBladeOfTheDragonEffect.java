package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemLostBladeOfTheDragonEffect {
  private ItemLostBladeOfTheDragonEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_lost_blade_of_the_dragon_effect}",
        "${spell.description.item_lost_blade_of_the_dragon_effect}",
        "0", 0, 0, 0, 0,
        false, false, "64kSpellIconFireBoost",
        null, null, 0, 0,
        null, null, 0, "10000", "0", 233,
        null, 10586, 1, 5, 1,
        "100", "0", "0", "0",
        0, 0, false, List.of(new SpellData.T4cEffect(9, List.of(new SpellData.T4cEffect.EffectParam(1, "10587"), new SpellData.T4cEffect.EffectParam(2, "OnAttackHit"), new SpellData.T4cEffect.EffectParam(3, "100"), new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
