package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheAdept {
  private RingOfTheAdept() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_adept",
        "${item.ring_of_the_adept}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        510L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        25L,
        0.0d,
        false,
        false,
        false,
        41706,
        2,
        179,
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
        List.of(new ItemDefinition.ItemSpell(10721, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
