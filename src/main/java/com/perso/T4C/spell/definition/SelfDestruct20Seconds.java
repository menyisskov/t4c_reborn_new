package com.perso.T4C.spell.definition;

import com.perso.T4C.spell.SpellData;
import java.util.List;

public final class SelfDestruct20Seconds {
  private SelfDestruct20Seconds() {}

  public static SpellData definition() {
    return new SpellData(
        "${spell.self_destruct_20_seconds}",
        "",
        "0",
        0,
        0,
        0,
        1,
        false,
        false,
        "npc",
        null,
        null,
        0,
        0,
        null,
        null,
        0,
        "20",
        null,
        0,
        null,
        10797,
        0,
        0,
        2,
        "100",
        null,
        null,
        null,
        0,
        0,
        false,
        List.of());
  }
}
