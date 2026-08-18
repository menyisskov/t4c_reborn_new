package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfBlinking {
  private RingOfBlinking() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_blinking",
        "${item.ring_of_blinking}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        0L,
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
        41161,
        2,
        177,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10313, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
