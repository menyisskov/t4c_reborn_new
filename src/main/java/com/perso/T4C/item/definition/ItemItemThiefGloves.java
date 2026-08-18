package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemThiefGloves {
  private ItemItemThiefGloves() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.thief_gloves",
        "${item.thief_gloves}",
        BodyPart.LEFT_HAND,
        "PupLeatherGloveL",
        BodyPart.RIGHT_HAND,
        "PupLeatherGloveR",
        "64kInvLeatherArmorGloves",
        110L,
        3L,
        1.0d,
        0L,
        15L,
        0L,
        0L,
        0L,
        30L,
        0L,
        0.0d,
        false,
        false,
        false,
        40100,
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
            new ItemDefinition.ItemBoost(49, 6, "7", 15, 15),
            new ItemDefinition.ItemBoost(50, 9, "10", 15, 15),
            new ItemDefinition.ItemBoost(452, 10015, "5", 0, 0)),
        List.of(),
        false);
  }
}
