package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCentaurChest3 {
  private ItemItemCentaurChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.centaur_chest_3",
        "${item.centaur_chest_3}",
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
        41287,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Polished bone key",
        3,
        null,
        631,
        800,
        1600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Potion of mana",
                    "Mana elixir",
                    "Manastone",
                    "Potion of fury",
                    "Potion of nimbleness",
                    "Potion of clear thought",
                    "Potion of tranquility",
                    "Potion of fortitude",
                    "Scroll of recall")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Adamantite blade",
                    "Potion of clear thought",
                    "Mithril blade",
                    "High metal broadsword",
                    "Rough ruby",
                    "Hickory compound bow",
                    "Serious healing potion",
                    "Morningstar of the Sun",
                    "Critical healing potion"))),
        false);
  }
}
