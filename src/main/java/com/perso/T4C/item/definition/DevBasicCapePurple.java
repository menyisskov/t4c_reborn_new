package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DevBasicCapePurple {
  private DevBasicCapePurple() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_basic_cape_purple",
        "${item.dev_basic_cape_purple}",
        BodyPart.BACK,
        "NMS_NewCape01__pal3",
        null,
        null,
        "Inv_NMS_NewCape01__pal3",
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
        3219,
        2,
        933,
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
