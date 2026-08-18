package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest10 {
  private ItemItemChest10() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_10",
        "${item.chest_10}",
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
        40097,
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
        800,
        1400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Potion of mana",
                    "Potion of mana",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Leather gloves",
                    "Leather gloves",
                    "Leather gloves",
                    "Torch",
                    "Light healing potion",
                    "Light healing potion",
                    "Leather belt"))),
        false);
  }
}
