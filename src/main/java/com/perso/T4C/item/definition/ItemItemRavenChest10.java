package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest10 {
  private ItemItemRavenChest10() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_10",
        "${item.raven_chest_10}",
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
        40284,
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
        176,
        2800,
        3000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Leather gloves",
                    "Leather boots",
                    "Studded leather gloves",
                    "Studded leather helmet",
                    "Studded leather pants",
                    "Studded leather boots",
                    "Light healing potion",
                    "Healing potion",
                    "Scalemail helmet",
                    "Scalemail gauntlets",
                    "Ringmail boots",
                    "Ringmail gauntlets",
                    "Ringmail leggings",
                    "Ringmail helmet"))),
        false);
  }
}
