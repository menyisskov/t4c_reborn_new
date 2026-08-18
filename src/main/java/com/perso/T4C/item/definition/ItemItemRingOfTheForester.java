package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheForester {
  private ItemItemRingOfTheForester() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_forester",
        "${item.ring_of_the_forester}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        0L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        60L,
        15L,
        15L,
        0.0d,
        false,
        false,
        false,
        41641,
        5,
        178,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10673, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(916, 10016, "20", 0, 0),
            new ItemDefinition.ItemBoost(917, 10004, "10", 0, 0)),
        List.of(),
        false);
  }
}
