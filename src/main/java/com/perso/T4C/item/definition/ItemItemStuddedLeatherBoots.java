package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemStuddedLeatherBoots {
  private ItemItemStuddedLeatherBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.studded_leather_boots",
        "${item.studded_leather_boots}",
        BodyPart.FEET,
        "PupLeatherBoots",
        null,
        null,
        "64kInvLeatherArmorBoots",
        457L,
        4L,
        0.81d,
        3L,
        45L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40028,
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
