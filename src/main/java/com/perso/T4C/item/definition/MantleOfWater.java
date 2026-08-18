package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MantleOfWater {
  private MantleOfWater() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_water",
        "${item.mantle_of_water}",
        BodyPart.BODY,
        "PupWhiteRobe__pal2",
        null,
        null,
        "64kInvWhiteRobe__pal2",
        0L,
        5L,
        20.0d,
        25L,
        30L,
        0L,
        0L,
        0L,
        300L,
        75L,
        1.0d,
        false,
        false,
        false,
        3325,
        2,
        586,
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
