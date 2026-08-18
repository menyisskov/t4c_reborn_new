package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemShadowHelm {
  private ItemItemShadowHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.shadow_helm",
        "${item.shadow_helm}",
        BodyPart.HEAD,
        "WitchHat1",
        null,
        null,
        "inv_WitchHat1",
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
        3494,
        2,
        658,
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
