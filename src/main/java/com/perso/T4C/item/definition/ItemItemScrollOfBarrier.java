package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemScrollOfBarrier {
  private ItemItemScrollOfBarrier() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scroll_of_barrier",
        "${item.scroll_of_barrier}",
        null,
        null,
        null,
        null,
        "64kInvMisc1 3",
        210L,
        1L,
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
        41644,
        5,
        14,
        null,
        "0",
        0,
        1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10656, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
