package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemSteelSafe {
  private ItemItemSteelSafe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.steel_safe",
        "${item.steel_safe}",
        null,
        null,
        null,
        null,
        "64kInvVault",
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
        41483,
        3,
        459,
        null,
        "0",
        0,
        0,
        true,
        "Steel safe key",
        500,
        null,
        147,
        2500,
        2500,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Secret document"))),
        false);
  }
}
