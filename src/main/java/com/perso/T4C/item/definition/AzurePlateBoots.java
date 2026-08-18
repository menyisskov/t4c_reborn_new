package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AzurePlateBoots {
  private AzurePlateBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_boots",
        "${item.azure_plate_boots}",
        BodyPart.FEET,
        "PupPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
        0L,
        3L,
        4.995d,
        0L,
        100L,
        0L,
        0L,
        0L,
        50L,
        50L,
        0.0d,
        false,
        false,
        false,
        40852,
        2,
        265,
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
        List.of(
            new ItemDefinition.ItemBoost(524, 3, "8", 0, 0),
            new ItemDefinition.ItemBoost(525, 6, "8", 0, 0),
            new ItemDefinition.ItemBoost(526, 2, "8", 0, 0),
            new ItemDefinition.ItemBoost(527, 1, "8", 0, 0),
            new ItemDefinition.ItemBoost(528, 4, "8", 0, 0)),
        List.of(),
        false);
  }
}
