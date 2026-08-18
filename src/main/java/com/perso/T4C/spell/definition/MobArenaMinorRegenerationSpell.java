package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobArenaMinorRegenerationSpell {
  private MobArenaMinorRegenerationSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_arena_minor_regeneration_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "HealingSpell-", null, 0, 25,
        null, null, 0, null, null, 0,
        null, 10618, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
