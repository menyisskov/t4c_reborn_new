package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class GemOfDestiny {
  private GemOfDestiny() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_of_destiny",
        "${item.gem_of_destiny}",
        null,
        null,
        null,
        null,
        "64kInvDestinyGem",
        0L,
        0L,
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
        40112,
        5,
        203,
        null,
        "0",
        20,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10834, 0, 100)),
        List.of(),
        List.of(),
        true);
  }
}
