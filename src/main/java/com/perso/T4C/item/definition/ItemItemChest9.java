package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest9 {
  private ItemItemChest9() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_9",
        "${item.chest_9}",
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
        40096,
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
        132,
        1500,
        3200,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Studded leather gloves",
                    "Leather Helmet",
                    "Leather boots",
                    "Leather gloves",
                    "Studded leather pants",
                    "Leather Helmet",
                    "Leather boots",
                    "Light healing potion"))),
        false);
  }
}
