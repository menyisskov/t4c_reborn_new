package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ReshaChest {
  private ReshaChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.resha_chest",
        "${item.resha_chest}",
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
        41478,
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
        0,
        3000,
        3000,
        List.of(),
        List.of(),
        List.of(new ItemDefinition.ContainerLootGroup(List.of("Magic mirror"))),
        false);
  }
}
