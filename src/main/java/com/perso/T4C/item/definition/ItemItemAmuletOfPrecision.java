package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAmuletOfPrecision {
  private ItemItemAmuletOfPrecision() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.amulet_of_precision",
        "${item.amulet_of_precision}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 3",
        813L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        27L,
        26L,
        0.0d,
        false,
        false,
        false,
        40137,
        2,
        174,
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
        List.of(
            new ItemDefinition.ItemBoost(94, 8, "10", 0, 0),
            new ItemDefinition.ItemBoost(457, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(880, 10035, "10", 0, 0)),
        List.of(),
        false);
  }
}
