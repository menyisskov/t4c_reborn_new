package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPlateLeggings {
  private ItemItemPlateLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.plate_leggings",
        "${item.plate_leggings}",
        BodyPart.LEGS,
        "PupPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        6595L,
        12L,
        4.05d,
        9L,
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
        40206,
        2,
        266,
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
