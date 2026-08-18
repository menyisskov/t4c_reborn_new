package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DwarvenPlatemailGauntlets {
  private DwarvenPlatemailGauntlets() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dwarven_platemail_gauntlets",
        "${item.dwarven_platemail_gauntlets}",
        BodyPart.LEFT_HAND,
        "PupPlateGloveL",
        BodyPart.RIGHT_HAND,
        "PupPlateGloveR",
        "64kInvPlateGlove",
        10058L,
        8L,
        5.67d,
        11L,
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
        40344,
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
            new ItemDefinition.ItemBoost(216, 12, "4", 0, 0),
            new ItemDefinition.ItemBoost(217, 22, "4", 0, 0),
            new ItemDefinition.ItemBoost(218, 15, "4", 0, 0),
            new ItemDefinition.ItemBoost(219, 13, "4", 0, 0),
            new ItemDefinition.ItemBoost(220, 14, "4", 0, 0)),
        List.of(),
        false);
  }
}
