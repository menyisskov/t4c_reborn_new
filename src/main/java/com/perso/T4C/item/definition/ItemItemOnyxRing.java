package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemOnyxRing {
  private ItemItemOnyxRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.onyx_ring",
        "${item.onyx_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        1067L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        29L,
        31L,
        0.0d,
        false,
        false,
        false,
        40720,
        2,
        179,
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
            new ItemDefinition.ItemBoost(478, 6, "2", 0, 0),
            new ItemDefinition.ItemBoost(479, 1, "2", 0, 0),
            new ItemDefinition.ItemBoost(480, 3, "2", 0, 0),
            new ItemDefinition.ItemBoost(482, 4, "2", 0, 0),
            new ItemDefinition.ItemBoost(483, 2, "2", 0, 0)),
        List.of(),
        false);
  }
}
