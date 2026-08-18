package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMadChest1 {
  private ItemItemMadChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mad_chest_1",
        "${item.mad_chest_1}",
        null,
        null,
        null,
        null,
        "64kInvMisc 2 - All 1",
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
        41601,
        3,
        41,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        433,
        1000,
        1500,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "C1 Permit", "Broom", "Fork", "Candle", "Spoon", "Rolling pin", "C1 Permit")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Helmet of the Clairvoyant",
                    "Straight jacket 1",
                    "Scroll of detect hidden",
                    "Scroll of recall",
                    "Scroll of earthen strength",
                    "Scroll of barrier",
                    "Scroll of protection",
                    "Scroll of invisibility",
                    "Light healing potion",
                    "Serious healing potion",
                    "Critical healing potion"))),
        false);
  }
}
