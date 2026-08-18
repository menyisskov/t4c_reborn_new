package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMidnightShroud {
  private ItemItemMidnightShroud() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.midnight_shroud",
        "${item.midnight_shroud}",
        BodyPart.BACK,
        "PupRedCape",
        null,
        null,
        "64kInvRedCape",
        0L,
        2L,
        3.0d,
        0L,
        13L,
        0L,
        0L,
        70L,
        18L,
        35L,
        0.0d,
        false,
        false,
        false,
        41670,
        2,
        287,
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
            new ItemDefinition.ItemBoost(963, 10014, "15", 0, 0),
            new ItemDefinition.ItemBoost(964, 10016, "15", 0, 0),
            new ItemDefinition.ItemBoost(965, 22, "10", 0, 0)),
        List.of(),
        false);
  }
}
