package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilPlateBoots {
  private MithrilPlateBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_plate_boots",
        "${item.mithril_plate_boots}",
        BodyPart.FEET,
        "PupMithrilPlateFoot",
        null,
        null,
        "64kInvPlateArmorFeet",
        37824L,
        3L,
        17.145d,
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
        0,
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
            new ItemDefinition.ItemBoost(261, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(262, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(263, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(264, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(265, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
