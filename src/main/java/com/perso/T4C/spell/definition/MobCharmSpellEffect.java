package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class MobCharmSpellEffect {
  private MobCharmSpellEffect() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.mob_charm_spell_effect}",
        "${spell.description.mob_charm_spell_effect}",
        "0",
        0,
        0,
        0,
        0,
        false,
        false,
        "64kSpellIconDarkDrainArea",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "30000",
        "0",
        233,
        null,
        10473,
        6,
        5,
        1,
        "100",
        "0",
        "30000",
        "0",
        0,
        0,
        false,
        List.of());
  }
}
