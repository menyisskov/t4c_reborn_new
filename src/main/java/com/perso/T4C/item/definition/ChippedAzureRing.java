package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ChippedAzureRing {
  private ChippedAzureRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.chipped_azure_ring",
        "${item.chipped_azure_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 1",
        0L,
        1L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        100L,
        75L,
        0.0d,
        false,
        false,
        false,
        40859,
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
            new ItemDefinition.ItemBoost(554, 11, "100", 0, 0),
            new ItemDefinition.ItemBoost(555, 3, "15", 0, 0),
            new ItemDefinition.ItemBoost(556, 6, "15", 0, 0),
            new ItemDefinition.ItemBoost(557, 2, "15", 0, 0),
            new ItemDefinition.ItemBoost(558, 4, "15", 0, 0),
            new ItemDefinition.ItemBoost(559, 1, "15", 0, 0)),
        List.of(),
        false);
  }
}
