package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAncientPlatemailGauntlets {
  private ItemItemAncientPlatemailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ancient_platemail_gauntlets",
        "${item.ancient_platemail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
        38703L,
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
        40411,
        2,
        263,
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
            new ItemDefinition.ItemBoost(266, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(267, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(268, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(269, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(270, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
