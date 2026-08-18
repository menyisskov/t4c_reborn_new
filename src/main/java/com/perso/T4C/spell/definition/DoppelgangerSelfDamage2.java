package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class DoppelgangerSelfDamage2 {
  private DoppelgangerSelfDamage2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.doppelganger_self_damage_2}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "Flak1-", "Flak1-", 1, 1,
        null, null, 0, null, null, 0,
        null, 0, 0, 0, 0,
        null, null, null, null,
        0, 0, false, List.of());
  }
}
