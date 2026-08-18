package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMcChest4 {
  private ItemItemMcChest4() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_4",
        "${item.mc_chest_4}",
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
        41277,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Key of the Lost Soul",
        75,
        null,
        667,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Light healing potion",
                    "Healing potion",
                    "Potion of fury",
                    "Potion of nimbleness",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of fortitude",
                    "Potion of regeneration",
                    "Torch",
                    "Serious healing potion",
                    "Potion of mana",
                    "Mana elixir")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Mithril flail",
                    "Plate protector",
                    "Potion of fortitude",
                    "Potion of cure poison",
                    "Fine steel hand axe",
                    "Plate protector",
                    "Elven leather belt",
                    "High metal short sword",
                    "High metal flail",
                    "Scroll of recall",
                    "Large shield",
                    "Mana prism"))),
        false);
  }
}
