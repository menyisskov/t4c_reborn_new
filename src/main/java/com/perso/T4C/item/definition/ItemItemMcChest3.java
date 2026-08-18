package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemMcChest3 {
  private ItemItemMcChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mc_chest_3",
        "${item.mc_chest_3}",
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
        41276,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        null,
        65,
        null,
        640,
        1200,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Elven chainmail girdle",
                    "Elven chainmail gauntlets",
                    "Elven chainmail helmet",
                    "Elven chainmail boots",
                    "Ring of the rogue",
                    "Mithril dagger",
                    "Mithril blade",
                    "Cord of treachery",
                    "Potion of nimbleness",
                    "Light healing potion",
                    "Healing potion",
                    "Cord of treachery",
                    "Potion of nimbleness",
                    "Light healing potion",
                    "Healing potion"))),
        false);
  }
}
