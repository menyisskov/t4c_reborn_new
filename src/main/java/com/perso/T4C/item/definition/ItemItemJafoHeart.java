package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemJafoHeart {
  private ItemItemJafoHeart() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jafo_heart",
        "${item.jafo_heart}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 3",
        0L,
        2L,
        10.0d,
        0L,
        10L,
        0L,
        0L,
        0L,
        95L,
        110L,
        0.0d,
        false,
        false,
        false,
        41359,
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
            new ItemDefinition.ItemBoost(666, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(667, 1, "10", 0, 0),
            new ItemDefinition.ItemBoost(668, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(669, 6, "10", 0, 0),
            new ItemDefinition.ItemBoost(670, 2, "10", 0, 0)),
        List.of(),
        false);
  }
}
