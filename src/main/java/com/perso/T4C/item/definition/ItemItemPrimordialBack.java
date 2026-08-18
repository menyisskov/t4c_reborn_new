package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemPrimordialBack {
  private ItemItemPrimordialBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.primordial_back",
        "${item.primordial_back}",
        BodyPart.BACK,
        "NMS_NewCapeLogo01__pal6",
        null,
        null,
        "Inv_NMS_NewCapeLogo01__pal6",
        500000L,
        2L,
        40.0d,
        0L,
        300L,
        0L,
        0L,
        300L,
        0L,
        0L,
        1.0d,
        false,
        false,
        true,
        3443,
        2,
        954,
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
