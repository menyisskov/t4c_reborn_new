package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemSilverChest {
  private ItemItemSilverChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.silver_chest",
        "${item.silver_chest}",
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
        41481,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Dark iron key",
        80,
        null,
        34,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Light healing potion", "Healing potion", "Serious healing potion")),
            new ItemDefinition.ContainerLootGroup(List.of("Chipped bone key"))),
        false);
  }
}
