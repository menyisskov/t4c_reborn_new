package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemNoReturnChest2 {
  private ItemItemNoReturnChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.no_return_chest_2",
        "${item.no_return_chest_2}",
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
        41697,
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
        912,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of nimbleness",
                    "Elven leather boots",
                    "Gem of the Stars",
                    "High metal flail",
                    "High metal flail",
                    "Potion of tranquility",
                    "Elven leather boots",
                    "Potion of fortitude",
                    "Potion of fortitude",
                    "Chainmail girdle",
                    "Chainmail girdle",
                    "Potion of nimbleness",
                    "Hickory compound bow",
                    "Hickory compound bow",
                    "Elven leather gloves",
                    "Scroll of mana surge",
                    "Scroll of mana surge",
                    "Elven leather gloves",
                    "Elven leather gloves")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of orientation middle",
                    "Ring of the forester",
                    "Elven leather gloves",
                    "Large shield",
                    "Elm reflex bow",
                    "Scroll of protection",
                    "Elven leather helmet",
                    "Scroll of orientation middle",
                    "Elm reflex bow"))),
        false);
  }
}
