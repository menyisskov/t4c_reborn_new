package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDragonscaleLeggings {
  private ItemItemDragonscaleLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dragonscale_leggings",
        "${item.dragonscale_leggings}",
        BodyPart.LEGS,
        "PupChainMailLegs",
        null,
        null,
        "64kInvChainMailLegs",
        29920L,
        7L,
        7.65d,
        5L,
        180L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40574,
        2,
        268,
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
            new ItemDefinition.ItemBoost(251, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(252, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(253, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(254, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(255, 14, "5", 0, 0)),
        List.of(),
        false);
  }
}
