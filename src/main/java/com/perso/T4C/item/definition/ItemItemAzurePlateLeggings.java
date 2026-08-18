package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAzurePlateLeggings {
  private ItemItemAzurePlateLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_leggings",
        "${item.azure_plate_leggings}",
        BodyPart.LEGS,
        "PupPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        0L,
        4L,
        5.55d,
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
        40850,
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
        List.of(
            new ItemDefinition.ItemBoost(534, 3, "8", 0, 0),
            new ItemDefinition.ItemBoost(535, 6, "8", 0, 0),
            new ItemDefinition.ItemBoost(536, 2, "8", 0, 0),
            new ItemDefinition.ItemBoost(537, 1, "8", 0, 0),
            new ItemDefinition.ItemBoost(538, 4, "8", 0, 0)),
        List.of(),
        false);
  }
}
