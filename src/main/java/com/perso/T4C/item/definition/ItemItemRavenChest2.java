package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest2 {
  private ItemItemRavenChest2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_2",
        "${item.raven_chest_2}",
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
        40246,
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
        198,
        2180,
        3500,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Potion of mana",
                    "Flask of crystal water",
                    "Potion of mana",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion",
                    "Mana elixir"))),
        false);
  }
}
