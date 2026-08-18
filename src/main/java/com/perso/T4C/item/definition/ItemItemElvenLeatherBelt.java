package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenLeatherBelt {
  private ItemItemElvenLeatherBelt() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_leather_belt",
        "${item.elven_leather_belt}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        1339L,
        2L,
        1.2d,
        1L,
        75L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40736,
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
