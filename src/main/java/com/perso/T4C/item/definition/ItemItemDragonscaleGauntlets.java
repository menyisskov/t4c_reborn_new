package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDragonscaleGauntlets {
  private ItemItemDragonscaleGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dragonscale_gauntlets",
        "${item.dragonscale_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        26333L,
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
        40575,
        2,
        259,
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
            new ItemDefinition.ItemBoost(241, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(242, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(243, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(244, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(245, 14, "5", 0, 0)),
        List.of(),
        false);
  }
}
