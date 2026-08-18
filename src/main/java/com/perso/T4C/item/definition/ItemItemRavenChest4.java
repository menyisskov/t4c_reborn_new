package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest4 {
  private ItemItemRavenChest4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_4",
        "${item.raven_chest_4}",
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
        40278,
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
        230,
        2900,
        3100,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Ring of light",
                    "Potion of mana",
                    "Sapphire bracelet",
                    "Torch",
                    "Light healing potion",
                    "Healing potion"))),
        false);
  }
}
