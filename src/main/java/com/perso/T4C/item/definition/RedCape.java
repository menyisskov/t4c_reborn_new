package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RedCape {
  private RedCape() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.red_cape",
        "${item.red_cape}",
        BodyPart.BACK,
        "PupRedCape",
        null,
        null,
        "64kInvRedCape",
        233L,
        4L,
        0.0d,
        0L,
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
        40215,
        2,
        287,
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
