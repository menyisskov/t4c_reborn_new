package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemGemOfUnstableProtection {
  private ItemItemGemOfUnstableProtection() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gem_of_unstable_protection",
        "${item.gem_of_unstable_protection}",
        null,
        null,
        null,
        null,
        "64kInvGems 4",
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
        41873,
        5,
        167,
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
        List.of(new ItemDefinition.ItemSpell(10781, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
