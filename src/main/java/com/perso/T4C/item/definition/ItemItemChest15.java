package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemChest15 {
  private ItemItemChest15() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chest_15",
        "${item.chest_15}",
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
        40121,
        3,
        20,
        null,
        "0",
        0,
        0,
        true,
        "Iron key",
        25,
        null,
        34,
        3500,
        3500,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Book of Feylor"))),
        false);
  }
}
