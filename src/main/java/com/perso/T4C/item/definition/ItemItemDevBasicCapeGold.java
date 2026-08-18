package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDevBasicCapeGold {
  private ItemItemDevBasicCapeGold() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev_basic_cape_gold",
        "${item.dev_basic_cape_gold}",
        BodyPart.BACK,
        "NMS_NewCape01__pal6",
        null,
        null,
        "Inv_NMS_NewCape01__pal6",
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
        3215,
        2,
        936,
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
