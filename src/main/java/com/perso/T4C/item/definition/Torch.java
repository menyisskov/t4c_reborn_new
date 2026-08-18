package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class Torch {
  private Torch() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.torch",
        "${item.torch}",
        null,
        null,
        null,
        null,
        "64kInvTorch",
        5L,
        3L,
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
        40015,
        5,
        244,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10015, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
