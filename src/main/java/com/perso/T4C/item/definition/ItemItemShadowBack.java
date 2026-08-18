package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShadowBack {
  private ItemItemShadowBack() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_back",
        "${item.shadow_back}",
        BodyPart.BACK,
        "NMS_NewCapeLogo01__pal8",
        null,
        null,
        "Inv_NMS_NewCapeLogo01__pal8",
        500000L,
        2L,
        25.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        400L,
        75L,
        1.0d,
        false,
        false,
        true,
        3490,
        2,
        956,
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
