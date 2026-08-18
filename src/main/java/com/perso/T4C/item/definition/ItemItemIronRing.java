package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemIronRing {
  private ItemItemIronRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.iron_ring",
        "${item.iron_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        73L,
        1L,
        0.5d,
        0L,
        0L,
        0L,
        0L,
        0L,
        17L,
        18L,
        0.0d,
        false,
        false,
        false,
        40038,
        2,
        176,
        "0",
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
