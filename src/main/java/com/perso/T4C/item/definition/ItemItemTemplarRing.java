package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTemplarRing {
  private ItemItemTemplarRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.templar_ring",
        "${item.templar_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        3499L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        14L,
        66L,
        0.0d,
        false,
        false,
        false,
        41545,
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
            new ItemDefinition.ItemBoost(864, 4, "5", 0, 0),
            new ItemDefinition.ItemBoost(867, 23, "5", 0, 0),
            new ItemDefinition.ItemBoost(978, 22, "5", 0, 0)),
        List.of(),
        false);
  }
}
