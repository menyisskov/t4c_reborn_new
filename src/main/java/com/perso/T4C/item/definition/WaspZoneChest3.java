package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class WaspZoneChest3 {
  private WaspZoneChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wasp_zone_chest_3",
        "${item.wasp_zone_chest_3}",
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
        41158,
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
        672,
        800,
        1600,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Elven leather belt",
                    "Elven leather gloves",
                    "Elven leather boots",
                    "Elven leather helmet",
                    "Light healing potion",
                    "Healing potion",
                    "Serious healing potion",
                    "Critical healing potion",
                    "Potion of mana",
                    "Mana elixir",
                    "Manastone",
                    "Mana prism",
                    "Ring of the duellist"))),
        false);
  }
}
