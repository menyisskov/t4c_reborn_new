package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMakrshPtanghTeleportSpell {
  private MobMakrshPtanghTeleportSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_makrsh_ptangh_teleport_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        null, null, 0, 25,
        null, null, 0, null, null, 0,
        null, 10596, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
