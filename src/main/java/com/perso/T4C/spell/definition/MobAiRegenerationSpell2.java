package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobAiRegenerationSpell2 {
  private MobAiRegenerationSpell2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_ai_regeneration_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "HealingSpell-", "HealingSpell-", 0, 0,
        null, null, 0, null, null, 0,
        null, 10294, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
