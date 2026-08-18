package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGmSilverRingOfEngagement {
  private ItemItemGmSilverRingOfEngagement() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gm_silver_ring_of_engagement",
        "${item.gm_silver_ring_of_engagement}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        1500L,
        1L,
        2.0d,
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
        40704,
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
        List.of(),
        List.of(),
        false);
  }
}
