package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GladiatorsRing {
  private GladiatorsRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.gladiators_ring",
        "${item.gladiators_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 5",
        0L,
        1L,
        5.0d,
        0L,
        0L,
        500L,
        108L,
        75L,
        20L,
        25L,
        0.0d,
        false,
        false,
        false,
        41709,
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
        List.of(
            new ItemDefinition.ItemBoost(980, 8, "100", 0, 0),
            new ItemDefinition.ItemBoost(981, 10002, "25", 0, 0)),
        List.of(),
        false);
  }
}
