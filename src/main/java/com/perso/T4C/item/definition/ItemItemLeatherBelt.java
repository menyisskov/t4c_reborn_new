package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLeatherBelt {
  private ItemItemLeatherBelt() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.leather_belt",
        "${item.leather_belt}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        70L,
        2L,
        0.3d,
        1L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40733,
        2,
        235,
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
        List.of(),
        List.of(),
        false);
  }
}
