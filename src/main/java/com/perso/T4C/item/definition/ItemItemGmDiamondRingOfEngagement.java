package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmDiamondRingOfEngagement {
  private ItemItemGmDiamondRingOfEngagement() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_diamond_ring_of_engagement",
        "${item.gm_diamond_ring_of_engagement}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        4000L,
        1L,
        5.0d,
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
        40705,
        2,
        178,
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
        List.of(),
        List.of(),
        false);
  }
}
