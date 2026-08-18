package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheRanger {
  private RingOfTheRanger() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_ranger",
        "${item.ring_of_the_ranger}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        2871L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        50L,
        17L,
        13L,
        0.0d,
        false,
        false,
        false,
        41376,
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
        List.of(
            new ItemDefinition.ItemBoost(681, 10035, "25", 0, 0),
            new ItemDefinition.ItemBoost(682, 10014, "10", 0, 0)),
        List.of(),
        false);
  }
}
