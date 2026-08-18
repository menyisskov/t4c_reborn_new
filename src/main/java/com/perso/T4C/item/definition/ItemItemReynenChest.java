package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemReynenChest {
  private ItemItemReynenChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.reynen_chest",
        "${item.reynen_chest}",
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
        40274,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Reynen Key",
        200,
        null,
        223,
        1400,
        1400,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Royal Key 1", "Feather"))),
        false);
  }
}
