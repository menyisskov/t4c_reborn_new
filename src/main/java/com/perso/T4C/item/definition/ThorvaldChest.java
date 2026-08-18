package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ThorvaldChest {
  private ThorvaldChest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.thorvald_chest",
        "${item.thorvald_chest}",
        null,
        null,
        null,
        null,
        "64kInvChest",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3423,
        3,
        20,
        null,
        null,
        0,
        0,
        true,
        "bifrost key",
        0,
        null,
        500000,
        3600,
        3600,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
