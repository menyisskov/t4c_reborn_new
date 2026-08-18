package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class TameBeast {
  private TameBeast() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.tame_beast}",
        "${spell.tame_beast.desc}",
        "10", 0, 0, 0, 1,
        true, true, "64kSpellIconTameBeast",
        null, "HealingSpell-", 0, 0,
        null, null, 30, "5", null, 0,
        null, 0, 0, 2, 0,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
