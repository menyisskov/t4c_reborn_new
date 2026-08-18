package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheSeraph {
  private ItemItemRingOfTheSeraph() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_seraph",
        "${item.ring_of_the_seraph}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        0L,
        1L,
        10.0d,
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
        40923,
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
            new ItemDefinition.ItemBoost(683, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(684, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(685, 14, "10", 0, 0),
            new ItemDefinition.ItemBoost(686, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(687, 22, "10", 0, 0)),
        List.of(),
        false);
  }
}
