package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DoppelgangerSelfDamage32 {
  private DoppelgangerSelfDamage32() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.doppelganger_self_damage_3}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "Flak1-", "Flak1-", 1, 1,
        null, null, 0, null, null, 0,
        null, 10780, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
