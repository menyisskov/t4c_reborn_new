package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfConfidence {
  private RingOfConfidence() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_confidence",
        "${item.ring_of_confidence}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        533L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        17L,
        31L,
        0.0d,
        false,
        false,
        false,
        40082,
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
        List.of(
            new ItemDefinition.ItemBoost(494, 4, "5", 0, 0),
            new ItemDefinition.ItemBoost(495, 8, "5", 0, 0),
            new ItemDefinition.ItemBoost(883, 10035, "5", 0, 0)),
        List.of(),
        false);
  }
}
