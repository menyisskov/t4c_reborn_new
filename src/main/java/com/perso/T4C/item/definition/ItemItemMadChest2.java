package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMadChest2 {
  private ItemItemMadChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mad_chest_2",
        "${item.mad_chest_2}",
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
        41602,
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
        231,
        1000,
        1500,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Broom",
                    "Fork",
                    "Candle",
                    "Spoon",
                    "Rolling pin",
                    "Potion of tranquility",
                    "Potion of clear thought",
                    "Potion of fortitude",
                    "Potion of nimbleness",
                    "Potion of fury",
                    "Potion of lesser fire resistance",
                    "Potion of lesser earth resistance",
                    "Potion of lesser water resistance",
                    "Potion of lesser air resistance",
                    "Potion of lesser protection from evil",
                    "Mana prism",
                    "Mana elixir",
                    "Potion of mana",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Bloodstained broadsword",
                    "Straight jacket 1",
                    "Scroll of protection",
                    "Scroll of recall",
                    "Scroll of barrier",
                    "Scroll of earthen strength",
                    "Scroll of minor combat sense",
                    "Scroll of detect hidden"))),
        false);
  }
}
