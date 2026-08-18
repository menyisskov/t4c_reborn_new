package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMcChest5 {
  private ItemItemMcChest5() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_5",
        "${item.mc_chest_5}",
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
        41278,
        3,
        238,
        null,
        "0",
        0,
        0,
        true,
        "Key of the Banished",
        50,
        null,
        667,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Mithril broadsword",
                    "Rough agate",
                    "Potion of fortitude",
                    "Mithril chainmail boots",
                    "High metal flail",
                    "Serious healing potion",
                    "Light healing potion",
                    "High metal short sword",
                    "Potion of tranquility",
                    "Hickory compound bow",
                    "Oak recurve bow",
                    "Rough limestone",
                    "Fine steel dagger",
                    "Critical healing potion")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Torch",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Scroll of recall",
                    "Potion of cure rabies",
                    "Potion of cure disease",
                    "Potion of cure poison"))),
        false);
  }
}
