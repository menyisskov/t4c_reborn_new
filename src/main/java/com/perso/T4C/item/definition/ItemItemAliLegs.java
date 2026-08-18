package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemAliLegs {
  private ItemItemAliLegs() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_legs",
        "${item.ali_legs}",
        BodyPart.LEGS,
        "PupLegsClothSet1",
        null,
        null,
        "64kInvClothSet1Legs",
        500000L,
        2L,
        9.0d,
        0L,
        70L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3891,
        2,
        284,
        null,
        null,
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
