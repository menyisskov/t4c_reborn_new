package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheInitiate {
  private RingOfTheInitiate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_initiate",
        "${item.ring_of_the_initiate}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 3",
        650L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        23L,
        25L,
        0.0d,
        false,
        false,
        false,
        40750,
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
            new ItemDefinition.ItemBoost(504, 6, "1", 0, 0),
            new ItemDefinition.ItemBoost(505, 2, "1", 0, 0),
            new ItemDefinition.ItemBoost(506, 1, "1", 0, 0),
            new ItemDefinition.ItemBoost(507, 4, "1", 0, 0),
            new ItemDefinition.ItemBoost(510, 3, "1", 0, 0)),
        List.of(),
        false);
  }
}
