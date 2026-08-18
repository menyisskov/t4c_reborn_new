package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfChance {
  private ItemItemRingOfChance() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_chance",
        "${item.ring_of_chance}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        1311L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        20L,
        20L,
        0.0d,
        false,
        false,
        false,
        40043,
        2,
        179,
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
        List.of(new ItemDefinition.ItemBoost(7, 7, "5", 0, 0)),
        List.of(),
        false);
  }
}
