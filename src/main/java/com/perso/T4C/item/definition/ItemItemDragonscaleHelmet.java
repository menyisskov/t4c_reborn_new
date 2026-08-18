package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDragonscaleHelmet {
  private ItemItemDragonscaleHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dragonscale_helmet",
        "${item.dragonscale_helmet}",
        BodyPart.HEAD,
        "PupChainMailCoif",
        null,
        null,
        "64kInvChainMailHelm",
        28525L,
        5L,
        6.63d,
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
        40573,
        2,
        270,
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
            new ItemDefinition.ItemBoost(246, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(247, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(248, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(249, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(250, 14, "5", 0, 0)),
        List.of(),
        false);
  }
}
