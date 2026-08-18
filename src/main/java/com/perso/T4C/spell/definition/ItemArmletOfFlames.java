package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemArmletOfFlames {
  private ItemArmletOfFlames() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_armlet_of_flames}",
        "${spell.description.item_armlet_of_flames}",
        "self.level/4",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconFireBoost",
        "FireWipe-",
        null,
        0,
        0,
        "Mind Shield.wav",
        null,
        0,
        "60000",
        "0",
        233,
        null,
        10782,
        0,
        5,
        1,
        "if(equipcount(41860)=1?100:0)",
        "0",
        "0",
        "0",
        30005,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10783"),
                    new SpellData.T4cEffect.EffectParam(2, "OnHit"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "100")))));
  }
}
