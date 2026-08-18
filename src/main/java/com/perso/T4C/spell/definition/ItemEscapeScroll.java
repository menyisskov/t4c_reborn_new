package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class ItemEscapeScroll {
  private ItemEscapeScroll() {}

  public static SpellData definition() {
    return new SpellData(
        "${item.escape_scroll}",
        "",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "0",
        "0",
        0,
        null,
        10672,
        0,
        5,
        1,
        "100",
        "0",
        "0",
        "0",
        30012,
        0,
        false,
        List.of(
            new SpellData.T4cEffect(
                7,
                List.of(
                    new SpellData.T4cEffect.EffectParam(1, "709"),
                    new SpellData.T4cEffect.EffectParam(2, "1340"),
                    new SpellData.T4cEffect.EffectParam(3, "0")))));
  }
}
