package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class NpcSelfDestruct2Minutes2 {
  private NpcSelfDestruct2Minutes2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.npc_self_destruct_2_minutes}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        null, null, 0, 0,
        null, null, 0, null, null, 0,
        null, 10752, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
