package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemOrdinaryChest {
  private ItemItemOrdinaryChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ordinary_chest",
        "${item.ordinary_chest}",
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
        41479,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        21,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(List.of("Light healing potion")),
            new ItemDefinition.ContainerLootGroup(List.of("Shiny metal key"))),
        false);
  }
}
