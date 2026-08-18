package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPlatinumRing {
  private ItemItemPlatinumRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.platinum_ring",
        "${item.platinum_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        2600L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        37L,
        41L,
        0.0d,
        false,
        false,
        false,
        40168,
        2,
        177,
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
            new ItemDefinition.ItemBoost(487, 6, "3", 0, 0),
            new ItemDefinition.ItemBoost(488, 2, "3", 0, 0),
            new ItemDefinition.ItemBoost(489, 4, "3", 0, 0),
            new ItemDefinition.ItemBoost(491, 3, "3", 0, 0),
            new ItemDefinition.ItemBoost(492, 1, "3", 0, 0)),
        List.of(),
        false);
  }
}
