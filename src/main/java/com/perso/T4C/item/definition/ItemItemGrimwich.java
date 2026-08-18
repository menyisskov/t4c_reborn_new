package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemGrimwich {
  private ItemItemGrimwich() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.grimwich",
        "${item.grimwich}",
        BodyPart.WEAPON,
        "V2_Viperine",
        null,
        null,
        "Inv_V2_Viperine",
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
        1.8181818181818181d,
        false,
        false,
        false,
        3521,
        1,
        686,
        "1",
        "550",
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
