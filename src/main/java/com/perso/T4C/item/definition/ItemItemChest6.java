package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest6 {
  private ItemItemChest6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_6",
        "${item.chest_6}",
        null,
        null,
        null,
        null,
        "64kInvChest",
        0L,
        10000L,
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
        40091,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        34,
        750,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Iron ring",
                    "Iron ring",
                    "Iron ring",
                    "Torch",
                    "Torch",
                    "Light healing potion",
                    "Healing potion",
                    "Ring of confidence",
                    "Ring of confidence"))),
        false);
  }
}
