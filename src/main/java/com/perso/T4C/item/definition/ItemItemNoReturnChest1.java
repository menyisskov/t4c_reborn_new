package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemNoReturnChest1 {
  private ItemItemNoReturnChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.no_return_chest_1",
        "${item.no_return_chest_1}",
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
        41696,
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
        789,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of recall",
                    "Gem of the Moon",
                    "Potion of nimbleness",
                    "High metal short sword",
                    "Scroll of recall",
                    "Elven leather gloves",
                    "Elven leather gloves",
                    "Hickory flatbow",
                    "Scroll of detect invisible",
                    "Light healing potion",
                    "Scroll of minor combat sense",
                    "Light healing potion",
                    "Hickory flatbow",
                    "Potion of nimbleness",
                    "Potion of nimbleness")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of orientation center",
                    "Scalemail boots",
                    "Elven leather boots",
                    "Elm recurve bow",
                    "Potion of clear thought",
                    "Bow of the Spiders",
                    "Scroll of mana surge"))),
        false);
  }
}
