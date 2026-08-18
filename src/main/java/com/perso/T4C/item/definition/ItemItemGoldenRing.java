package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGoldenRing {
  private ItemItemGoldenRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.golden_ring",
        "${item.golden_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        650L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        22L,
        26L,
        0.0d,
        false,
        false,
        false,
        40039,
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
        List.of(new ItemDefinition.ItemBoost(6, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
