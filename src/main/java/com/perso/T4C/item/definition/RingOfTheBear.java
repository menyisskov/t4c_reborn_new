package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheBear {
  private RingOfTheBear() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_bear",
        "${item.ring_of_the_bear}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        437L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        20L,
        23L,
        0.0d,
        false,
        false,
        false,
        40040,
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
        List.of(new ItemDefinition.ItemBoost(10, 3, "5", 0, 0)),
        List.of(),
        false);
  }
}
