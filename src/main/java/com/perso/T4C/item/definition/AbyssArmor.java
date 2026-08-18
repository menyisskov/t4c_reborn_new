package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class AbyssArmor {
  private AbyssArmor() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.abyss_armor",
        "${item.abyss_armor}",
        BodyPart.BODY,
        "ManLichRobeOri",
        null,
        null,
        "Inv_LichRobeOri",
        500000L,
        2L,
        45.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        3480,
        2,
        923,
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
