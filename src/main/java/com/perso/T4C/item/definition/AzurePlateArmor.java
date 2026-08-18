package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AzurePlateArmor {
  private AzurePlateArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_armor",
        "${item.azure_plate_armor}",
        BodyPart.BODY,
        "PupPlateBody",
        null,
        null,
        "64kInvPlateArmorSleeves",
        0L,
        5L,
        16.75d,
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
        40849,
        2,
        264,
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
            new ItemDefinition.ItemBoost(519, 3, "10", 0, 0),
            new ItemDefinition.ItemBoost(520, 6, "10", 0, 0),
            new ItemDefinition.ItemBoost(521, 2, "10", 0, 0),
            new ItemDefinition.ItemBoost(522, 1, "10", 0, 0),
            new ItemDefinition.ItemBoost(523, 4, "10", 0, 0)),
        List.of(),
        false);
  }
}
