package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemElvenLeatherLeggings {
  private ItemItemElvenLeatherLeggings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_leather_leggings",
        "${item.elven_leather_leggings}",
        BodyPart.LEGS,
        "PupLeatherPants",
        null,
        null,
        "64kInvLeatherArmorLegs",
        1992L,
        5L,
        1.8d,
        2L,
        75L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40507,
        2,
        261,
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
