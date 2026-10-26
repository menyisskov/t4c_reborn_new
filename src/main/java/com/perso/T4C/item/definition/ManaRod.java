package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

/** Zhakar's lost rod from the Kraanian Egg quest. */
public final class ManaRod {
  private ManaRod() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mana_rod",
        "${item.mana_rod}",
        null,
        null,
        null,
        null,
        "64kInvSimpleStaff",
        0L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41999,
        6,
        14,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
