package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientPlateProtector {
  private ItemItemAncientPlateProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_plate_protector",
        "${item.ancient_plate_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        29328L,
        2L,
        12.7d,
        0L,
        300L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40745,
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
