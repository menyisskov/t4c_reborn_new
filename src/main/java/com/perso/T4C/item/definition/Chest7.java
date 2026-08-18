package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class Chest7 {
  private Chest7() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_7",
        "${item.chest_7}",
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
        40092,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Dark key",
        40,
        null,
        200,
        10000,
        80000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ring of darkness",
                    "Ring of darkness",
                    "Ring of darkness",
                    "Polished long sword",
                    "Elm longbow",
                    "Ring of darkness",
                    "Ring of darkness",
                    "Ring of darkness"))),
        false);
  }
}
