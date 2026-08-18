package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCapeNoel {
  private ItemItemCapeNoel() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cape_noel",
        "${item.cape_noel}",
        BodyPart.BACK,
        "NMS_NewCapeGarde01",
        null,
        null,
        "Inv_NMS_NewCapeGarde01",
        0L,
        0L,
        20.0d,
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
        4070,
        2,
        940,
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
