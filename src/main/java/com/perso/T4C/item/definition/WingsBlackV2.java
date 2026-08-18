package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class WingsBlackV2 {
  private WingsBlackV2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wings_black_v2",
        "${item.wings_black_v2}",
        BodyPart.BACK,
        "NMSX6BlackWings",
        null,
        null,
        "Inv_NMSX6BlackWing",
        200000L,
        2L,
        20.0d,
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
        3535,
        2,
        1146,
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
