package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHighCleristRobes {
  private ItemItemHighCleristRobes() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.high_clerist_robes",
        "${item.high_clerist_robes}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        0L,
        5L,
        15.0d,
        25L,
        25L,
        0L,
        0L,
        0L,
        25L,
        155L,
        0.0d,
        false,
        false,
        false,
        40549,
        2,
        425,
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
            new ItemDefinition.ItemBoost(582, 2, "10", 0, 0),
            new ItemDefinition.ItemBoost(583, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(584, 23, "20", 0, 0)),
        List.of(),
        false);
  }
}
