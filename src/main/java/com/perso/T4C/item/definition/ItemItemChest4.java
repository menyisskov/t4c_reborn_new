package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest4 {
  private ItemItemChest4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_4",
        "${item.chest_4}",
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
        40089,
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
        72,
        760,
        1840,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Studded leather helmet",
                    "Leather boots",
                    "Potion of mana",
                    "Iron ring",
                    "Torch",
                    "Light healing potion",
                    "Light healing potion",
                    "Leather belt",
                    "Rusted dirk",
                    "Steel reinforced club",
                    "Ashwood reflex bow"))),
        false);
  }
}
