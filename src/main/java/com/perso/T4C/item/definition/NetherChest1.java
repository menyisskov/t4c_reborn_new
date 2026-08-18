package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class NetherChest1 {
  private NetherChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.nether_chest_1",
        "${item.nether_chest_1}",
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
        41289,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Key of Ascension",
        75,
        null,
        343,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Healing potion",
                    "Mithril chainmail helmet",
                    "Healing potion",
                    "Plate protector",
                    "Tower shield",
                    "Potion of nimbleness",
                    "Hickory compound bow",
                    "Healing potion",
                    "Potion of cure poison",
                    "Mithril chainmail girdle",
                    "Potion of tranquility",
                    "Mithril long sword",
                    "Plate boots"))),
        false);
  }
}
