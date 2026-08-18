package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BloodstoneRing {
  private BloodstoneRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.bloodstone_ring",
        "${item.bloodstone_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 4",
        650L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        30L,
        18L,
        0.0d,
        false,
        false,
        false,
        40722,
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
        List.of(new ItemDefinition.ItemBoost(446, 1, "5", 0, 0)),
        List.of(),
        false);
  }
}
