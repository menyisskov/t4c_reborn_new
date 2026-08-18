package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DoppelgangerSelfDamage22 {
  private DoppelgangerSelfDamage22() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.doppelganger_self_damage_2}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "Flak1-", "Flak1-", 1, 1,
        null, null, 0, null, null, 0,
        null, 10779, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
