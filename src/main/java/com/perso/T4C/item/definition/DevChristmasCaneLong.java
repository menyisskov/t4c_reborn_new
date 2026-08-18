package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DevChristmasCaneLong {
  private DevChristmasCaneLong() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_christmas_cane_long",
        "${item.dev_christmas_cane_long}",
        BodyPart.WEAPON,
        "NM_CanneNoelLongue",
        null,
        null,
        "Inv_CanneNoel",
        0L,
        0L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3212,
        1,
        1090,
        "0",
        "0",
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
