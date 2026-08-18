package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class RavenChest8 {
  private RavenChest8() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_8",
        "${item.raven_chest_8}",
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
        40282,
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
        165,
        2300,
        2600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Torch",
                    "Potion of mana",
                    "Sapphire bracelet",
                    "Ring of light",
                    "Light healing potion",
                    "Light healing potion",
                    "Potion of mana",
                    "Mana elixir"))),
        false);
  }
}
