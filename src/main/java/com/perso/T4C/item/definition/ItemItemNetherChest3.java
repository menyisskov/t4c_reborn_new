package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemNetherChest3 {
  private ItemItemNetherChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.nether_chest_3",
        "${item.nether_chest_3}",
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
        41291,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Key of Ascension",
        85,
        null,
        642,
        1200,
        2000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of cure rabies",
                    "High metal flail",
                    "Light healing potion",
                    "High metal short sword",
                    "High metal scimitar",
                    "Tower shield",
                    "Dwarven platemail gauntlets",
                    "Dwarven platemail helmet",
                    "Dwarven plate protector",
                    "Dwarven platemail boots",
                    "Oak longbow",
                    "Adamantite blade",
                    "Hyper potion of fury",
                    "Hyper potion of clear thought",
                    "Hyper potion of fortitude",
                    "Hyper potion of tranquility",
                    "Hyper potion of nimbleness",
                    "Rough agate",
                    "Plate boots",
                    "Plate helmet"))),
        false);
  }
}
