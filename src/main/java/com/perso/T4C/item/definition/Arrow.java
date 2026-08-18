package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Arrow {
  private Arrow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.arrow",
        "${item.arrow}",
        BodyPart.WEAPON2,
        null,
        null,
        null,
        "64kInvMisc 7 - All 4",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        true,
        41138,
        8,
        84,
        "10",
        "0",
        0,
        0,
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
