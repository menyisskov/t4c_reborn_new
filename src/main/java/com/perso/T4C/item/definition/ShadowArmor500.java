package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ShadowArmor500 {
  private ShadowArmor500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_armor_500",
        "${item.shadow_armor_500}",
        BodyPart.BODY,
        "ManLichRobeNoir",
        null,
        null,
        "Inv_LichRobeBlack",
        500000L,
        2L,
        95.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        4116,
        2,
        924,
        null,
        null,
        0,
        -1,
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
