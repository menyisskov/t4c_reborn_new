package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemManaPrism {
  private ItemItemManaPrism() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mana_prism",
        "${item.mana_prism}",
        null,
        null,
        null,
        null,
        "64kInvGems 3",
        10000L,
        5L,
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
        40361,
        5,
        166,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10213, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
