package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemEmeraldPowerFocus {
  private ItemItemEmeraldPowerFocus() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.emerald_power_focus",
        "${item.emerald_power_focus}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "InvEmeraldFocus",
        0L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        28L,
        65L,
        0.0d,
        false,
        false,
        false,
        41301,
        2,
        570,
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
            new ItemDefinition.ItemBoost(648, 19, "25", 0, 0),
            new ItemDefinition.ItemBoost(649, 16, "-15", 0, 0),
            new ItemDefinition.ItemBoost(650, 12, "-10", 0, 0),
            new ItemDefinition.ItemBoost(651, 15, "-10", 0, 0),
            new ItemDefinition.ItemBoost(652, 13, "-10", 0, 0),
            new ItemDefinition.ItemBoost(653, 14, "-10", 0, 0)),
        List.of(),
        false);
  }
}
