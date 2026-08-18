package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheHealer {
  private ItemItemRingOfTheHealer() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_healer",
        "${item.ring_of_the_healer}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        1260L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        50L,
        0.0d,
        false,
        false,
        false,
        41541,
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
        List.of(new ItemDefinition.ItemSpell(10474, 0, 100)),
        List.of(new ItemDefinition.ItemBoost(863, 4, "5", 0, 0)),
        List.of(),
        false);
  }
}
