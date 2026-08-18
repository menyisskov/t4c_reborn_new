package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemJadeRingOfSorcery {
  private ItemItemJadeRingOfSorcery() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.jade_ring_of_sorcery",
        "${item.jade_ring_of_sorcery}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        13996L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        90L,
        65L,
        0.0d,
        false,
        false,
        false,
        40640,
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
        List.of(
            new ItemDefinition.ItemBoost(473, 16, "10", 0, 0),
            new ItemDefinition.ItemBoost(474, 24, "10", 0, 0),
            new ItemDefinition.ItemBoost(475, 18, "10", 0, 0),
            new ItemDefinition.ItemBoost(476, 19, "10", 0, 0),
            new ItemDefinition.ItemBoost(477, 17, "10", 0, 0)),
        List.of(),
        false);
  }
}
