package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobLordOfTheShopsTeleportSpell2 {
  private MobLordOfTheShopsTeleportSpell2() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_lord_of_the_shops_teleport_spell}",
        "",
        "0", 0, 0, 0, 1,
        false, false, "npc",
        null, null, 0, 0,
        null, null, 0, null, null, 0,
        null, 10725, 0, 0, 2,
        "100", null, null, null,
        0, 0, false, List.of());
  }
}
