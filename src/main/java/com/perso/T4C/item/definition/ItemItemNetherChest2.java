package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemNetherChest2 {
  private ItemItemNetherChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.nether_chest_2",
        "${item.nether_chest_2}",
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
        41290,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Key of Ascension",
        80,
        null,
        545,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Critical healing potion",
                    "Serious healing potion",
                    "High metal flail",
                    "Mithril shield",
                    "Potion of clear thought",
                    "High metal broadsword",
                    "Potion of fortitude",
                    "Mithril flail",
                    "Mithril chainmail girdle",
                    "Mithril broadsword",
                    "Potion of fury",
                    "Potion of tranquility",
                    "Prismatic blade",
                    "Plate gauntlets")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of recall",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Potion of regeneration",
                    "Potion of mana",
                    "Mana elixir",
                    "Manastone",
                    "Mana prism",
                    "Potion of cure rabies",
                    "Potion of cure disease",
                    "Potion of cure poison"))),
        false);
  }
}
