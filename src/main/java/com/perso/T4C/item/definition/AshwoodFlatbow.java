package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AshwoodFlatbow {
  private AshwoodFlatbow() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ashwood_flatbow",
        "${item.ashwood_flatbow}",
        BodyPart.WEAPON,
        "PupShortBow",
        null,
        null,
        "64kInvShortBow",
        9L,
        7L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1000.0d,
        false,
        true,
        false,
        41174,
        9,
        86,
        "1d3+arrow_dmg",
        "1000",
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
