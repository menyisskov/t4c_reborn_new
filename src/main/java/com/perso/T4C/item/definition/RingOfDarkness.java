package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfDarkness {
  private RingOfDarkness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_darkness",
        "${item.ring_of_darkness}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        73L,
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
        40749,
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
        List.of(),
        List.of(),
        false);
  }
}
