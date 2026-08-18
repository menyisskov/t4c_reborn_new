package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDevBasicCapeRed {
  private ItemItemDevBasicCapeRed() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_basic_cape_red",
        "${item.dev_basic_cape_red}",
        BodyPart.BACK,
        "NMS_NewCape01",
        null,
        null,
        "Inv_NMS_NewCape01",
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
        3220,
        2,
        931,
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
