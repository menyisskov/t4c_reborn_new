package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobOracleAssistantSelfHealSpell {
  private MobOracleAssistantSelfHealSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_oracle_assistant_self_heal_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "HealingSpell-", "HealingSpell-", 0, 0,
        null, null, 0, null, null, 0,
        null, 0, 0, 0, 0,
        null, null, null, null,
        0, 0, false, List.of());
  }
}
