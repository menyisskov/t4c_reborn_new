package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ElvenLeatherHelmet {
  private ElvenLeatherHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.elven_leather_helmet",
        "${item.elven_leather_helmet}",
        BodyPart.HEAD,
        "PupLeatherHelm",
        null,
        null,
        "64kInvLeatherArmorHelm",
        1900L,
        3L,
        1.56d,
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
        40481,
        2,
        11,
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
