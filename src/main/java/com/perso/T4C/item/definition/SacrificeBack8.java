package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SacrificeBack8 {
  private SacrificeBack8() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_back8",
        "${item.sacrifice_back8}",
        BodyPart.BACK,
        "PupSeraphDarkWings__pal2",
        null,
        null,
        "64kIconCape",
        2000000L,
        2L,
        25.0d,
        0L,
        150L,
        0L,
        0L,
        310L,
        310L,
        0L,
        1.0d,
        false,
        false,
        true,
        3949,
        2,
        922,
        null,
        null,
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
