package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWarlockRing {
  private ItemItemWarlockRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.warlock_ring",
        "${item.warlock_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "64kInvRings 5",
        1178L,
        1L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        53L,
        15L,
        0.0d,
        false,
        false,
        false,
        40103,
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
        List.of(new ItemDefinition.ItemBoost(500, 1, "15", 0, 0)),
        List.of(),
        false);
  }
}
