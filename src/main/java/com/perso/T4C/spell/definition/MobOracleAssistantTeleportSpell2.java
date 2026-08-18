package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobOracleAssistantTeleportSpell2 {
  private MobOracleAssistantTeleportSpell2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_oracle_assistant_teleport_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        null, null, 0, 0,
        null, null, 0, null, null, 0,
        null, 10471, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
