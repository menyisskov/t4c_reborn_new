package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class RavenChest9 {
  private RavenChest9() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_9",
        "${item.raven_chest_9}",
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
        40283,
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
        140,
        1700,
        1900,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Healing potion",
                    "Healing potion",
                    "Potion of mana",
                    "Mana elixir"))),
        false);
  }
}
