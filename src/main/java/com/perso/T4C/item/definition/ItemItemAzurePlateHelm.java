package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAzurePlateHelm {
  private ItemItemAzurePlateHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_helm",
        "${item.azure_plate_helm}",
        BodyPart.HEAD,
        "PupPlateHelm",
        null,
        null,
        "64kInvPlateArmorHelm",
        0L,
        3L,
        4.81d,
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
        40854,
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
        List.of(
            new ItemDefinition.ItemBoost(544, 3, "8", 0, 0),
            new ItemDefinition.ItemBoost(545, 6, "8", 0, 0),
            new ItemDefinition.ItemBoost(546, 2, "8", 0, 0),
            new ItemDefinition.ItemBoost(547, 1, "8", 0, 0),
            new ItemDefinition.ItemBoost(548, 4, "8", 0, 0)),
        List.of(),
        false);
  }
}
