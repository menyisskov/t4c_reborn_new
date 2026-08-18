package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfPureFaith {
  private RingOfPureFaith() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_pure_faith",
        "${item.ring_of_pure_faith}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        5200L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        15L,
        90L,
        0.0d,
        false,
        false,
        false,
        40905,
        2,
        176,
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
        List.of(new ItemDefinition.ItemBoost(570, 4, "30", 0, 0)),
        List.of(),
        false);
  }
}
