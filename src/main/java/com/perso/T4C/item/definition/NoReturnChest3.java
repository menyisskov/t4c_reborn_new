package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class NoReturnChest3 {
  private NoReturnChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.no_return_chest_3",
        "${item.no_return_chest_3}",
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
        41698,
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
        871,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Mithril chainmail girdle",
                    "Hickory recurve bow",
                    "Gem of the Sun",
                    "Mithril chainmail girdle",
                    "Scroll of detect hidden",
                    "Scroll of detect hidden",
                    "Hickory recurve bow",
                    "Potion of tranquility",
                    "Potion of tranquility",
                    "Hickory reflex bow",
                    "Hickory reflex bow",
                    "Scroll of barrier",
                    "Oak flatbow",
                    "Oak flatbow",
                    "Scroll of mana surge",
                    "Potion of tranquility",
                    "Potion of tranquility")),
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Scroll of orientation center",
                    "Grim sword of war",
                    "Cloak of the Skull",
                    "Escape scroll",
                    "Raincloak",
                    "Fine steel hand axe",
                    "Scroll of barrier",
                    "Elven leather gloves",
                    "Escape scroll",
                    "Elven leather gloves",
                    "Scroll of barrier"))),
        false);
  }
}
