package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest2 {
  private ItemItemChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_2",
        "${item.chest_2}",
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
        40087,
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
        5,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Torch",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion",
                    "Rusted dirk",
                    "Rusted dirk",
                    "Rusted dirk",
                    "Rusted long sword",
                    "Ashwood longbow")),
            new ItemDefinition.ContainerLootGroup(List.of("Torch", "Light healing potion"))),
        false);
  }
}
