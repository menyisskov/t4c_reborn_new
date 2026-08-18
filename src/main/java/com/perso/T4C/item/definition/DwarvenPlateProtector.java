package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DwarvenPlateProtector {
  private DwarvenPlateProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_plate_protector",
        "${item.dwarven_plate_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        7627L,
        6L,
        4.2d,
        8L,
        160L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40742,
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
