package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemArchiArmor500 {
  private ItemItemArchiArmor500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.archi_armor_500",
        "${item.archi_armor_500}",
        BodyPart.BODY,
        "ManLichRobeRouge",
        null,
        null,
        "Inv_LichRobeRed",
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
        4104,
        2,
        930,
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
