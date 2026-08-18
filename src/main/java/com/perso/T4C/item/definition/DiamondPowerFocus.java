package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DiamondPowerFocus {
  private DiamondPowerFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.diamond_power_focus",
        "${item.diamond_power_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvDiamondFocus",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        46L,
        46L,
        0.0d,
        false,
        false,
        false,
        41300,
        2,
        569,
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
            new ItemDefinition.ItemBoost(631, 16, "25", 0, 0),
            new ItemDefinition.ItemBoost(632, 19, "-15", 0, 0),
            new ItemDefinition.ItemBoost(633, 12, "-10", 0, 0),
            new ItemDefinition.ItemBoost(634, 15, "-10", 0, 0),
            new ItemDefinition.ItemBoost(635, 13, "-10", 0, 0),
            new ItemDefinition.ItemBoost(636, 14, "-10", 0, 0)),
        List.of(),
        false);
  }
}
