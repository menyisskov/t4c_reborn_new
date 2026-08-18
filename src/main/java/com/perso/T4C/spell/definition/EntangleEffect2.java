package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class EntangleEffect2 {
  private EntangleEffect2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.entangle_effect_2}",
        "",
        "0", 0, 0, 0, 0,
        false, false, "0",
        null, null, 0, 0,
        null, null, 0, "0", "0", 0,
        null, 10349, 2, 5, 1,
        "100", "0", "1750", "0",
        0, 0, false, List.of());
  }
}
