package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest11 {
  private ItemItemChest11() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_11",
        "${item.chest_11}",
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
        40098,
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
        70,
        950,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Torch",
                    "Torch",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion",
                    "Rusted dirk",
                    "Rusted long sword",
                    "Ashwood longbow")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Potion of mana",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion"))),
        false);
  }
}
