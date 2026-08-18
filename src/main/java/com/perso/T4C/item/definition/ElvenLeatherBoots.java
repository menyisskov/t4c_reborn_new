package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenLeatherBoots {
  private ElvenLeatherBoots() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_leather_boots",
        "${item.elven_leather_boots}",
        BodyPart.FEET,
        "PupLeatherBoots",
        null,
        null,
        "64kInvLeatherArmorBoots",
        1718L,
        3L,
        1.62d,
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
        40500,
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
