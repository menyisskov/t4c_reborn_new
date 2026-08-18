package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RingOfTheBerserker {
  private RingOfTheBerserker() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_berserker",
        "${item.ring_of_the_berserker}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        718L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        20L,
        25L,
        0.0d,
        false,
        false,
        false,
        41554,
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
        List.of(
            new ItemDefinition.ItemSpell(10485, 0, 100),
            new ItemDefinition.ItemSpell(10485, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(874, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(875, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(876, 9, "-25", 0, 0)),
        List.of(),
        false);
  }
}
