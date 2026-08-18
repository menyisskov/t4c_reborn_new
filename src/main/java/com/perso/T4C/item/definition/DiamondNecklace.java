package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DiamondNecklace {
  private DiamondNecklace() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.diamond_necklace",
        "${item.diamond_necklace}",
        BodyPart.NECK,
        null,
        null,
        null,
        "64kInvNecklace 1",
        991L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        29L,
        29L,
        0.0d,
        false,
        false,
        false,
        40048,
        2,
        172,
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
            new ItemDefinition.ItemBoost(18, 4, "5", 0, 0),
            new ItemDefinition.ItemBoost(19, 1, "5", 0, 0)),
        List.of(),
        false);
  }
}
