package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class Chest8 {
  private Chest8() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_8",
        "${item.chest_8}",
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
        40094,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Iron key",
        30,
        null,
        190,
        2500,
        5000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ring of darkness",
                    "Iron ring",
                    "Torch",
                    "Torch",
                    "Torch",
                    "Ring of confidence"))),
        false);
  }
}
