package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPlateProtector {
  private ItemItemPlateProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.plate_protector",
        "${item.plate_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        4407L,
        6L,
        2.7d,
        6L,
        125L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40740,
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
