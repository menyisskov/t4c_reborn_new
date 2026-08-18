package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLeatherBoots {
  private ItemItemLeatherBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.leather_boots",
        "${item.leather_boots}",
        BodyPart.FEET,
        "PupLeatherBoots",
        null,
        null,
        "64kInvLeatherArmorBoots",
        82L,
        3L,
        0.405d,
        1L,
        25L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40022,
        2,
        260,
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
