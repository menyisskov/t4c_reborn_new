package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class AncientRing {
  private AncientRing() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_ring",
        "${item.ancient_ring}",
        null,
        null,
        null,
        null,
        "64kInvRings 5",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41679,
        5,
        180,
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
        List.of(new ItemDefinition.ItemSpell(10687, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
