package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemScrollOfOrientationMiddle {
  private ItemScrollOfOrientationMiddle() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.item_scroll_of_orientation_middle}",
        "${spell.description.item_scroll_of_orientation_middle}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconNoneDefense",
        "HealingSpell-",
        null,
        0,
        0,
        "Healing.wav",
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10670,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30001,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                3,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "30438"),
                    new SpellData.T4cEffect.EffectParam(2, "1"))),
            new SpellData.T4cEffect(
                9,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "10671"),
                    new SpellData.T4cEffect.EffectParam(2, "OnTimer"),
                    new SpellData.T4cEffect.EffectParam(3, "100"),
                    new SpellData.T4cEffect.EffectParam(4, "30000")))));
  }
}
