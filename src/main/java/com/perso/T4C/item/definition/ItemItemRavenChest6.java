package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest6 {
  private ItemItemRavenChest6() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_6",
        "${item.raven_chest_6}",
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
        40280,
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
        245,
        5800,
        6000,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Potion of mana", "Healing potion", "Mana elixir"))),
        false);
  }
}
