package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemScarabProtector {
  private ItemItemScarabProtector() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.scarab_protector",
        "${item.scarab_protector}",
        BodyPart.BELT,
        null,
        null,
        null,
        "64kInvBelt",
        4730L,
        3L,
        1.9d,
        4L,
        100L,
        0L,
        0L,
        0L,
        53L,
        40L,
        0.0d,
        false,
        false,
        false,
        40178,
        2,
        235,
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
            new ItemDefinition.ItemBoost(119, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(496, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(497, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(498, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(499, 22, "5", 0, 0)),
        List.of(),
        false);
  }
}
