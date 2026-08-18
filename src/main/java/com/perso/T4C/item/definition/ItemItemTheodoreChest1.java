package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemTheodoreChest1 {
  private ItemItemTheodoreChest1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.theodore_chest_1",
        "${item.theodore_chest_1}",
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
        40240,
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
        130,
        7500,
        7500,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Light healing potion"))),
        false);
  }
}
