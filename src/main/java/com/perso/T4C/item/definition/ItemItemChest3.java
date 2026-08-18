package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest3 {
  private ItemItemChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_3",
        "${item.chest_3}",
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
        40088,
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
        45,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Leather boots",
                    "Leather Helmet",
                    "Leather gloves",
                    "Studded leather helmet",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion",
                    "Rusted dirk",
                    "Rusted dirk",
                    "Rusted long sword",
                    "Ashwood flatbow",
                    "Ashwood longbow"))),
        false);
  }
}
