package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTranslucentWristband {
  private ItemItemTranslucentWristband() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.translucent_wristband",
        "${item.translucent_wristband}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        0L,
        1L,
        10.0d,
        0L,
        50L,
        300L,
        100L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41715,
        2,
        237,
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
            new ItemDefinition.ItemBoost(1003, 12, "30", 0, 0),
            new ItemDefinition.ItemBoost(1004, 15, "-15", 0, 0)),
        List.of(),
        false);
  }
}
