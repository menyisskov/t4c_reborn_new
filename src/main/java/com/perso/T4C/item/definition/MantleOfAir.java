package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MantleOfAir {
  private MantleOfAir() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_air",
        "${item.mantle_of_air}",
        BodyPart.BODY,
        "PupWhiteRobe__pal7",
        null,
        null,
        "64kInvWhiteRobe__pal7",
        0L,
        5L,
        20.0d,
        25L,
        30L,
        0L,
        0L,
        0L,
        175L,
        175L,
        1.0d,
        false,
        false,
        false,
        3329,
        2,
        591,
        null,
        "0",
        0,
        -1,
        false,
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
