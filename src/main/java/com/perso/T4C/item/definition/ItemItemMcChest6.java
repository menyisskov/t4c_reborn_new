package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMcChest6 {
  private ItemItemMcChest6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_6",
        "${item.mc_chest_6}",
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
        41279,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Key of the Banished",
        100,
        null,
        670,
        1800,
        2100,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Key of the Forgotten"))),
        false);
  }
}
