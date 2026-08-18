package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemFlaskOfBluishLiquid {
  private ItemItemFlaskOfBluishLiquid() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.flask_of_bluish_liquid",
        "${item.flask_of_bluish_liquid}",
        null,
        null,
        null,
        null,
        "64kInvBlueFlask",
        10L,
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
        40003,
        5,
        236,
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
        List.of(new ItemDefinition.ItemSpell(10004, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
