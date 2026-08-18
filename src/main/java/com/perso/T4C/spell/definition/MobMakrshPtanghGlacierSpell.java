package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobMakrshPtanghGlacierSpell {
  private MobMakrshPtanghGlacierSpell() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_makrsh_ptangh_glacier_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        "64kSpellEnergyBallBlue-", "64kSpellGlacier-", 0, 25,
        null, null, 0, null, null, 0,
        null, 10709, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
