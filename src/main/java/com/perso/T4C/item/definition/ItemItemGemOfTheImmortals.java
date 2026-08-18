package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemGemOfTheImmortals {
  private ItemItemGemOfTheImmortals() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_of_the_immortals",
        "${item.gem_of_the_immortals}",
        null,
        null,
        null,
        null,
        "64kInvDarkGem",
        1L,
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
        41874,
        5,
        474,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10785, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
