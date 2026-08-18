package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheDuellist {
  private ItemItemRingOfTheDuellist() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_duellist",
        "${item.ring_of_the_duellist}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 2",
        5200L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        30L,
        50L,
        15L,
        0L,
        0.0d,
        false,
        false,
        false,
        41375,
        2,
        177,
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
            new ItemDefinition.ItemBoost(678, 8, "25", 0, 0),
            new ItemDefinition.ItemBoost(679, 9, "25", 0, 0),
            new ItemDefinition.ItemBoost(680, 10008, "10", 0, 0)),
        List.of(),
        false);
  }
}
