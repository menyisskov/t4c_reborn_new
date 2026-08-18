package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest1 {
  private ItemItemChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_1",
        "${item.chest_1}",
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
        40086,
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
        34,
        700,
        1800,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of(
                    "Leather boots",
                    "Potion of mana",
                    "Torch",
                    "Torch",
                    "Leather gloves",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Light healing potion",
                    "Healing potion",
                    "Rusted dirk",
                    "Ashwood flatbow"))),
        false);
  }
}
