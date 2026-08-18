package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfAccuracy {
  private ItemItemRingOfAccuracy() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_accuracy",
        "${item.ring_of_accuracy}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        792L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        29L,
        29L,
        0.0d,
        false,
        false,
        false,
        40723,
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
            new ItemDefinition.ItemBoost(447, 6, "5", 0, 0),
            new ItemDefinition.ItemBoost(448, 8, "15", 0, 0),
            new ItemDefinition.ItemBoost(879, 10035, "15", 0, 0)),
        List.of(),
        false);
  }
}
