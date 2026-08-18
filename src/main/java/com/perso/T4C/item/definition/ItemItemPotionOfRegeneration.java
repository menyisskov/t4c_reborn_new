package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemPotionOfRegeneration {
  private ItemItemPotionOfRegeneration() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_regeneration",
        "${item.potion_of_regeneration}",
        null,
        null,
        null,
        null,
        "64kInvMisc 9 - All 2",
        1666L,
        2L,
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
        40237,
        5,
        97,
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
        List.of(new ItemDefinition.ItemSpell(10110, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
