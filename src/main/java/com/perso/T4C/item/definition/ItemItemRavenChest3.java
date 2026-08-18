package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemRavenChest3 {
  private ItemItemRavenChest3() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.raven_chest_3",
        "${item.raven_chest_3}",
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
        40247,
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
        121,
        1975,
        3100,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Ring of light", "Ring of darkness", "Ring of confidence"))),
        false);
  }
}
