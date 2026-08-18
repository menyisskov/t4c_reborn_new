package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemClothPants {
  private ItemItemClothPants() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloth_pants",
        "${item.cloth_pants}",
        BodyPart.LEGS,
        "PupLegsClothSet1",
        null,
        null,
        "64kInvClothSet1Legs",
        0L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40213,
        2,
        284,
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
        List.of(),
        List.of(),
        false);
  }
}
