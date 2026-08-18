package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRocArmor {
  private ItemItemRocArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.roc_armor",
        "${item.roc_armor}",
        BodyPart.BODY,
        "ManLichRobeGreen",
        null,
        null,
        "Inv_LichRobeGreen",
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
        3497,
        2,
        927,
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
