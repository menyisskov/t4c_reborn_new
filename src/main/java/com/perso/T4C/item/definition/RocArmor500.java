package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RocArmor500 {
  private RocArmor500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.roc_armor_500",
        "${item.roc_armor_500}",
        BodyPart.BODY,
        "ManLichRobeGreen",
        null,
        null,
        "Inv_LichRobeGreen",
        500000L,
        2L,
        95.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        75L,
        400L,
        1.0d,
        false,
        false,
        true,
        4128,
        2,
        927,
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
