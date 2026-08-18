package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest5 {
  private ItemItemRavenChest5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_5",
        "${item.raven_chest_5}",
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
        40279,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        142,
        3000,
        3200,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ring of light",
                    "Potion of mana",
                    "Torch",
                    "Sapphire bracelet",
                    "Potion of mana",
                    "Healing potion",
                    "Healing potion",
                    "Mana elixir"))),
        false);
  }
}
