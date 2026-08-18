package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemDantalirChest {
  private ItemItemDantalirChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dantalir_chest",
        "${item.dantalir_chest}",
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
        41680,
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
        302,
        1400,
        1400,
        List.of(),
        List.of(),
        List.of(
            new ItemDefinition.ContainerLootGroup(
                List.of("Scroll of recall", "Finely crafted drum"))),
        false);
  }
}
