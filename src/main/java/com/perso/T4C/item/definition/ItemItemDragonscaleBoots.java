package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDragonscaleBoots {
  private ItemItemDragonscaleBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dragonscale_boots",
        "${item.dragonscale_boots}",
        BodyPart.FEET,
        "PupBlackLeatherBoots",
        null,
        null,
        "64kInvBlackLeatherBoots",
        25735L,
        5L,
        6.885d,
        4L,
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
        40576,
        2,
        288,
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
            new ItemDefinition.ItemBoost(236, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(237, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(238, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(239, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(240, 14, "5", 0, 0)),
        List.of(),
        false);
  }
}
