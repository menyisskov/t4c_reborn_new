package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRedempteurBack {
  private ItemItemRedempteurBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_back",
        "${item.redempteur_back}",
        BodyPart.BACK,
        "NMS_NewCape01__pal9",
        null,
        null,
        "Inv_NMS_NewCape01__pal9",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        3618,
        2,
        939,
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
