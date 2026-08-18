package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWindArmor500 {
  private ItemItemWindArmor500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wind_armor_500",
        "${item.wind_armor_500}",
        BodyPart.BODY,
        "ManLichRobeGold",
        null,
        null,
        "Inv_LichRobeGold",
        500000L,
        2L,
        95.0d,
        0L,
        500L,
        0L,
        0L,
        0L,
        125L,
        350L,
        1.0d,
        false,
        false,
        true,
        4140,
        2,
        926,
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
