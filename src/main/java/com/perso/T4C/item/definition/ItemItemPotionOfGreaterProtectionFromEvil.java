package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemPotionOfGreaterProtectionFromEvil {
  private ItemItemPotionOfGreaterProtectionFromEvil() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.potion_of_greater_protection_from_evil",
        "${item.potion_of_greater_protection_from_evil}",
        null,
        null,
        null,
        null,
        "64kInvPotions 1",
        1000L,
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
        40619,
        5,
        256,
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
        List.of(new ItemDefinition.ItemSpell(10277, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
