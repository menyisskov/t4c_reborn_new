package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMcChest2 {
  private ItemItemMcChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_2",
        "${item.mc_chest_2}",
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
        41275,
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
        632,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Elven chainmail girdle",
                    "Potion of fortitude",
                    "Light healing potion",
                    "Mithril dagger",
                    "Potion of nimbleness",
                    "Oak reflex bow",
                    "Scroll of recall",
                    "Light healing potion",
                    "Elven leather boots",
                    "Elven leather helmet",
                    "Potion of fury",
                    "Plate helmet"))),
        false);
  }
}
