package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemQuiverOfBrutality {
  private ItemItemQuiverOfBrutality() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.quiver_of_brutality",
        "${item.quiver_of_brutality}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kIconQuiver",
        22000L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        60L,
        300L,
        0L,
        0L,
        1.0d,
        false,
        true,
        true,
        3311,
        8,
        454,
        "1d50+80",
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(),
        List.of(),
        List.of(),
        false);
  }
}
