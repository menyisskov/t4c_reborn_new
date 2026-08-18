package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AllmightRing {
  private AllmightRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.allmight_ring",
        "${item.allmight_ring}",
        BodyPart.RING1,
        null,
        null,
        null,
        "InvRingCutRed",
        100000000L,
        0L,
        100.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3626,
        2,
        567,
        null,
        null,
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
        List.of(),
        List.of(),
        false);
  }
}
