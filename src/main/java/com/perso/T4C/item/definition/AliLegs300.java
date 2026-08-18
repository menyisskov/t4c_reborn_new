package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliLegs300 {
  private AliLegs300() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_legs_300",
        "${item.ali_legs_300}",
        BodyPart.LEGS,
        "PupLegsClothSet1",
        null,
        null,
        "64kInvClothSet1Legs",
        500000L,
        2L,
        29.0d,
        0L,
        270L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        4152,
        2,
        284,
        null,
        null,
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
