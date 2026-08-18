package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AzurePlateGauntlets {
  private AzurePlateGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_gauntlets",
        "${item.azure_plate_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
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
        40851,
        2,
        263,
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
            new ItemDefinition.ItemBoost(529, 3, "8", 0, 0),
            new ItemDefinition.ItemBoost(530, 6, "8", 0, 0),
            new ItemDefinition.ItemBoost(531, 2, "8", 0, 0),
            new ItemDefinition.ItemBoost(532, 1, "8", 0, 0),
            new ItemDefinition.ItemBoost(533, 4, "8", 0, 0)),
        List.of(),
        false);
  }
}
