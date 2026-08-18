package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class StuddedLeatherHelmet {
  private StuddedLeatherHelmet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.studded_leather_helmet",
        "${item.studded_leather_helmet}",
        BodyPart.HEAD,
        "PupLeatherHelm",
        null,
        null,
        "64kInvLeatherArmorHelm",
        503L,
        4L,
        0.78d,
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
        40025,
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
