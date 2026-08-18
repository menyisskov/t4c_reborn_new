package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MantleOfStone {
  private MantleOfStone() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_stone",
        "${item.mantle_of_stone}",
        BodyPart.BODY,
        "PupWhiteRobe__pal8",
        null,
        null,
        "64kInvWhiteRobe__pal8",
        0L,
        5L,
        20.0d,
        25L,
        30L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        false,
        3313,
        2,
        592,
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
