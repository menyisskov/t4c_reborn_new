package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRedempteurHelm {
  private ItemItemRedempteurHelm() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_helm",
        "${item.redempteur_helm}",
        BodyPart.HEAD,
        "V2_Haume01",
        null,
        null,
        "Inv_V2_Haume01",
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
        3614,
        2,
        680,
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
