package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class RavenChest1 {
  private RavenChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_1",
        "${item.raven_chest_1}",
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
        40245,
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
        148,
        2100,
        3500,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Leather gloves",
                    "Leather boots",
                    "Leather boots",
                    "Studded leather helmet",
                    "Studded leather gloves",
                    "Ringmail boots",
                    "Ringmail gauntlets",
                    "Ringmail helmet"))),
        false);
  }
}
