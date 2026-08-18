package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfLight {
  private ItemItemRingOfLight() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_light",
        "${item.ring_of_light}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        396L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        25L,
        25L,
        0.0d,
        false,
        false,
        false,
        40041,
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
        List.of(new ItemDefinition.ItemBoost(81, 11, "100", 0, 0)),
        List.of(),
        false);
  }
}
