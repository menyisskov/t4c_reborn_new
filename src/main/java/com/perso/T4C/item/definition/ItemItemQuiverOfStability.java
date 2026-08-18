package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemQuiverOfStability {
  private ItemItemQuiverOfStability() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quiver_of_stability",
        "${item.quiver_of_stability}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        1L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        30L,
        220L,
        0L,
        0L,
        0.0d,
        false,
        true,
        true,
        41861,
        8,
        454,
        "10",
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
            new ItemDefinition.ItemBoost(1022, 6, "25", 0, 0),
            new ItemDefinition.ItemBoost(1023, 10035, "100", 0, 0)),
        List.of(),
        false);
  }
}
