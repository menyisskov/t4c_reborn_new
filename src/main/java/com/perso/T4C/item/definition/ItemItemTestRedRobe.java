package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTestRedRobe {
  private ItemItemTestRedRobe() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.test_red_robe",
        "${item.test_red_robe}",
        BodyPart.BODY,
        "PupRedRobe",
        null,
        null,
        "64kInvRedRobe",
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
        41414,
        2,
        423,
        null,
        "0",
        0,
        0,
        false,
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
