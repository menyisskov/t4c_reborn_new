package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRustedChest {
  private ItemItemRustedChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rusted_chest",
        "${item.rusted_chest}",
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
        41480,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Shiny metal key",
        80,
        null,
        38,
        3000,
        3000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Light healing potion", "Healing potion")),
            new ItemDefinition.ContainerLootGroup(List.of("Dark iron key"))),
        false);
  }
}
