package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheNewbi {
  private RingOfTheNewbi() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_newbi",
        "${item.ring_of_the_newbi}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 5",
        0L,
        0L,
        5.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        false,
        3261,
        2,
        180,
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
