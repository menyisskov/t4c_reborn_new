package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class JanderRingOfNimbleness {
  private JanderRingOfNimbleness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jander_ring_of_nimbleness",
        "${item.jander_ring_of_nimbleness}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        8531L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        63L,
        55L,
        0.0d,
        false,
        false,
        false,
        41364,
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
            new ItemDefinition.ItemBoost(675, 6, "25", 0, 0),
            new ItemDefinition.ItemBoost(676, 9, "25", 0, 0)),
        List.of(),
        false);
  }
}
