package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobOracleAssistantSelfDamageSpell2 {
  private MobOracleAssistantSelfDamageSpell2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_oracle_assistant_self_damage_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "Flak1-", "Flak1-", 1, 1,
        null, null, 0, null, null, 0,
        null, 10717, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
