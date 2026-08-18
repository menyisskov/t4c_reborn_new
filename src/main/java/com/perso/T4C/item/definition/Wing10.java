package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Wing10 {
  private Wing10() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wing10",
        "${item.wing10}",
        BodyPart.BACK,
        "NMS_nDechuWings__pal2",
        null,
        null,
        "64kIconCape",
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
        1.0d,
        false,
        false,
        true,
        3644,
        2,
        920,
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
