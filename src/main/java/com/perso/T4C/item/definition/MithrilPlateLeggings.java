package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MithrilPlateLeggings {
  private MithrilPlateLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_plate_leggings",
        "${item.mithril_plate_leggings}",
        BodyPart.LEGS,
        "PupMithrilPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        43976L,
        5L,
        19.05d,
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
            new ItemDefinition.ItemBoost(276, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(277, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(278, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(279, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(280, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
