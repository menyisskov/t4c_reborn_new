package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMakrshPtanghBouldersSpell {
  private MobMakrshPtanghBouldersSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_makrsh_ptangh_boulders_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "64kSpellEnergyBallGreen-", "64kSpellBoulders-", 0, 25,
        null, null, 0, null, null, 0,
        null, 10708, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
