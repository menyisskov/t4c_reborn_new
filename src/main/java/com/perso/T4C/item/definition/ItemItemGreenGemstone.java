package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemGreenGemstone {
  private ItemItemGreenGemstone() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.green_gemstone",
        "${item.green_gemstone}",
        null,
        null,
        null,
        null,
        "64kInvGems 4",
        0L,
        1L,
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
        40538,
        6,
        167,
        null,
        "0",
        0,
        0,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(new ItemDefinition.ItemBoost(125, 11, "75", 0, 0)),
        List.of(),
        false);
  }
}
