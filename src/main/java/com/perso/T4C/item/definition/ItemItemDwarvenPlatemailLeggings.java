package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDwarvenPlatemailLeggings {
  private ItemItemDwarvenPlatemailLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_platemail_leggings",
        "${item.dwarven_platemail_leggings}",
        BodyPart.LEGS,
        "PupPlateLegs",
        null,
        null,
        "64kInvPlateArmorLegs",
        11424L,
        12L,
        6.3d,
        12L,
        160L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40521,
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
            new ItemDefinition.ItemBoost(226, 12, "4", 0, 0),
            new ItemDefinition.ItemBoost(227, 22, "4", 0, 0),
            new ItemDefinition.ItemBoost(228, 15, "4", 0, 0),
            new ItemDefinition.ItemBoost(229, 13, "4", 0, 0),
            new ItemDefinition.ItemBoost(230, 14, "4", 0, 0)),
        List.of(),
        false);
  }
}
