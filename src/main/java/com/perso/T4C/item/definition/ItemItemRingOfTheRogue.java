package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheRogue {
  private ItemItemRingOfTheRogue() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_rogue",
        "${item.ring_of_the_rogue}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        18837L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        30L,
        52L,
        41L,
        0.0d,
        false,
        false,
        false,
        41360,
        2,
        176,
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
            new ItemDefinition.ItemBoost(671, 6, "25", 0, 0),
            new ItemDefinition.ItemBoost(677, 10015, "10", 0, 0)),
        List.of(),
        false);
  }
}
