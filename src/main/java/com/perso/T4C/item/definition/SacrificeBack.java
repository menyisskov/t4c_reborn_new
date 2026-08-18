package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SacrificeBack {
  private SacrificeBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sacrifice_back",
        "${item.sacrifice_back}",
        BodyPart.BACK,
        "NMS_NewCapeLogo01__pal6",
        null,
        null,
        "Inv_NMS_NewCapeLogo01__pal6",
        500000L,
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
        3924,
        2,
        954,
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
