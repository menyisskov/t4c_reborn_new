package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMordenthalDungeon2 {
  private ItemItemMordenthalDungeon2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mordenthal_dungeon_2",
        "${item.mordenthal_dungeon_2}",
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
        41168,
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
        556,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of recall",
                    "Large shield",
                    "Elven chainmail helmet",
                    "Fine steel hand axe",
                    "Oak flatbow",
                    "Rough carnelian",
                    "Critical healing potion",
                    "Potion of nimbleness",
                    "Plate helmet"))),
        false);
  }
}
