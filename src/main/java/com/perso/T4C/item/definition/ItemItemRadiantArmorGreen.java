package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRadiantArmorGreen {
  private ItemItemRadiantArmorGreen() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.radiant_armor_green",
        "${item.radiant_armor_green}",
        BodyPart.BODY,
        "PupWhiteRobe__pal3",
        null,
        null,
        "64kInvWhiteRobe__pal3",
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
        3870,
        2,
        587,
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
