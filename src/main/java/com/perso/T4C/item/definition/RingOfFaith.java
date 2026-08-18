package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfFaith {
  private RingOfFaith() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_faith",
        "${item.ring_of_faith}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        1150L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        15L,
        65L,
        0.0d,
        false,
        false,
        false,
        40594,
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
        List.of(new ItemDefinition.ItemBoost(133, 4, "20", 0, 0)),
        List.of(),
        false);
  }
}
