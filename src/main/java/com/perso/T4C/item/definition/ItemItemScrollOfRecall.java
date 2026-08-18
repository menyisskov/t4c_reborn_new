package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import java.util.List;

public final class ItemItemScrollOfRecall {
  private ItemItemScrollOfRecall() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scroll_of_recall",
        "${item.scroll_of_recall}",
        null,
        null,
        null,
        null,
        "64kInvMisc1 2",
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
        true,
        false,
        41407,
        5,
        127,
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
        List.of(new ItemDefinition.ItemSpell(10398, 0, 100)),
        List.of(),
        List.of(),
        false);
  }
}
