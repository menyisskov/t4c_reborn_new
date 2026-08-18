package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfThePriest {
  private ItemItemRingOfThePriest() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_priest",
        "${item.ring_of_the_priest}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 5",
        3000L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        75L,
        0.0d,
        false,
        false,
        false,
        41707,
        2,
        180,
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
        List.of(new ItemDefinition.ItemSpell(10723, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
