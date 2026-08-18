package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWillowispRing {
  private ItemItemWillowispRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.willowisp_ring",
        "${item.willowisp_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        343L,
        1L,
        0.0d,
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
        41830,
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
        List.of(new ItemDefinition.ItemBoost(1015, 11, "100", 0, 0)),
        List.of(),
        false);
  }
}
