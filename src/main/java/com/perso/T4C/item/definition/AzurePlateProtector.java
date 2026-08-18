package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AzurePlateProtector {
  private AzurePlateProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.azure_plate_protector",
        "${item.azure_plate_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        0L,
        3L,
        3.7d,
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
        40853,
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
        List.of(
            new ItemDefinition.ItemBoost(539, 3, "8", 0, 0),
            new ItemDefinition.ItemBoost(540, 6, "8", 0, 0),
            new ItemDefinition.ItemBoost(541, 2, "8", 0, 0),
            new ItemDefinition.ItemBoost(542, 1, "8", 0, 0),
            new ItemDefinition.ItemBoost(543, 4, "8", 0, 0)),
        List.of(),
        false);
  }
}
