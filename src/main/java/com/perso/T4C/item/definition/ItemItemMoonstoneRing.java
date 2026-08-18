package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMoonstoneRing {
  private ItemItemMoonstoneRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.moonstone_ring",
        "${item.moonstone_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        1585L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        19L,
        46L,
        0.0d,
        false,
        false,
        false,
        40721,
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
        List.of(new ItemDefinition.ItemBoost(445, 4, "10", 0, 0)),
        List.of(),
        false);
  }
}
