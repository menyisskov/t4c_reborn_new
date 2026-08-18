package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRingOfTheLion {
  private ItemItemRingOfTheLion() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ring_of_the_lion",
        "${item.ring_of_the_lion}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        0L,
        1L,
        0.0d,
        0L,
        22L,
        0L,
        30L,
        26L,
        25L,
        25L,
        0.0d,
        false,
        false,
        false,
        41381,
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
            new ItemDefinition.ItemBoost(893, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(894, 10027, "5", 0, 0),
            new ItemDefinition.ItemBoost(895, 10008, "5", 0, 0),
            new ItemDefinition.ItemBoost(896, 10002, "5", 0, 0),
            new ItemDefinition.ItemBoost(897, 10029, "5", 0, 0),
            new ItemDefinition.ItemBoost(898, 10001, "5", 0, 0)),
        List.of(),
        false);
  }
}
