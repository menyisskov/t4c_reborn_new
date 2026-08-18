package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class Chest13 {
  private Chest13() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_13",
        "${item.chest_13}",
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
        40101,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Iron key",
        40,
        null,
        24,
        2000,
        3500,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Iron ring", "Iron ring", "Iron ring", "Iron ring", "Torch", "Torch")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion"))),
        false);
  }
}
