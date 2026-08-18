package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemV2Sp01 {
  private ItemItemV2Sp01() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.v2_sp01",
        "${item.v2_sp01}",
        BodyPart.WEAPON,
        "V2_Special01",
        null,
        null,
        "Inv_V2_Sp01",
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
        false,
        3103,
        1,
        756,
        "1000",
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
