package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemCentaurChest1 {
  private ItemItemCentaurChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.centaur_chest_1",
        "${item.centaur_chest_1}",
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
        41285,
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
        435,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Serious healing potion",
                    "Rough ruby",
                    "Elven chainmail girdle",
                    "Large shield",
                    "Light healing potion",
                    "Hickory reflex bow",
                    "Elm recurve bow",
                    "Healing potion",
                    "Potion of fortitude",
                    "Potion of clear thought",
                    "Elven leather gloves",
                    "Prismatic blade",
                    "Healing potion",
                    "Potion of fortitude",
                    "Potion of clear thought",
                    "Elven leather gloves"))),
        false);
  }
}
