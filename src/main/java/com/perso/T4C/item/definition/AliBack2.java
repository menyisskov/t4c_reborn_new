package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AliBack2 {
  private AliBack2() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.ali_back_2",
        "${item.ali_back_2}",
        BodyPart.BACK,
        "ButterFlyWing",
        null,
        null,
        "Inv_ButterFlyWing",
        2000000L,
        2L,
        6.0d,
        0L,
        70L,
        0L,
        350L,
        350L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3895,
        2,
        667,
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
