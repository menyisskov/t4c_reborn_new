package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class PlateHelmet {
  private PlateHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.plate_helmet",
        "${item.plate_helmet}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        6289L,
        8L,
        3.51d,
        8L,
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
        40205,
        2,
        267,
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
