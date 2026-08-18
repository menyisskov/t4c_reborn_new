package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemDev1 {
  private ItemItemDev1() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.dev1",
        "${item.dev1}",
        BodyPart.HEAD,
        "T4CP_Casquette",
        null,
        null,
        "Inv_T4CP_Casquette",
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
        3242,
        2,
        890,
        null,
        null,
        0,
        0,
        false,
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
