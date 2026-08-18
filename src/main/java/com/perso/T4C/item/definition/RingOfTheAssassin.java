package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheAssassin {
  private RingOfTheAssassin() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_assassin",
        "${item.ring_of_the_assassin}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        4710L,
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
        41616,
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
        List.of(
            new ItemDefinition.ItemBoost(904, 10016, "5", 0, 0),
            new ItemDefinition.ItemBoost(905, 10014, "5", 0, 0),
            new ItemDefinition.ItemBoost(906, 10027, "5", 0, 0),
            new ItemDefinition.ItemBoost(907, 9, "10", 0, 0),
            new ItemDefinition.ItemBoost(908, 6, "10", 0, 0)),
        List.of(),
        false);
  }
}
