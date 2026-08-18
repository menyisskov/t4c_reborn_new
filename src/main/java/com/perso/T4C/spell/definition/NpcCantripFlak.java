package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class NpcCantripFlak {
  private NpcCantripFlak() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.npc_cantrip_flak}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "Flak1-", "Flak1-", 0, 0,
        null, null, 0, null, null, 0,
        null, 0, 0, 0, 0,
        null, null, null, null,
        0, 0, false, List.of());
  }
}
