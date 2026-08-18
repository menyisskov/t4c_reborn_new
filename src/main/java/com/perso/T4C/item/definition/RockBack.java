package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RockBack {
  private RockBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.rock_back",
        "${item.rock_back}",
        BodyPart.BACK,
        "NMS_NewCapeLogo01__pal7",
        null,
        null,
        "Inv_NMS_NewCapeLogo01__pal7",
        500000L,
        2L,
        25.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        3498,
        2,
        955,
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
