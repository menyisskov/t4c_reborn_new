package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRadiantArmor {
  private ItemItemRadiantArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_armor",
        "${item.radiant_armor}",
        BodyPart.BODY,
        "ManLichRobeBlanc",
        null,
        null,
        "Inv_LichRobeWhite",
        500000L,
        2L,
        45.0d,
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
        3513,
        2,
        925,
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
