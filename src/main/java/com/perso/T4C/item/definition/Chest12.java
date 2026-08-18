package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class Chest12 {
  private Chest12() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_12",
        "${item.chest_12}",
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
        40099,
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
        30,
        1200,
        2100,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ring of light",
                    "Iron ring",
                    "Leather belt",
                    "Leather belt",
                    "Leather belt",
                    "Ring of confidence",
                    "Iron ring",
                    "Iron ring")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Torch",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Rusted long sword",
                    "Iron staff",
                    "Ashwood longbow"))),
        false);
  }
}
