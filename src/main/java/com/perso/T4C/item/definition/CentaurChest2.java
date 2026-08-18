package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class CentaurChest2 {
  private CentaurChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.centaur_chest_2",
        "${item.centaur_chest_2}",
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
        41286,
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
        543,
        800,
        1600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Elven chainmail gauntlets",
                    "Elm recurve bow",
                    "High metal dagger",
                    "Serious healing potion",
                    "Mithril dagger",
                    "Potion of cure rabies",
                    "Fine steel dagger",
                    "Elven leather gloves",
                    "Light healing potion",
                    "Potion of clear thought",
                    "Plate protector",
                    "Potion of cure disease"))),
        false);
  }
}
