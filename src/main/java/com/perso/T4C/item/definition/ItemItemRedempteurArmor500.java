package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemRedempteurArmor500 {
  private ItemItemRedempteurArmor500() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.redempteur_armor_500",
        "${item.redempteur_armor_500}",
        BodyPart.BODY,
        "PupSpikeLeatherBody",
        null,
        null,
        "InvSpikedLeatherBody",
        500000L,
        2L,
        145.0d,
        0L,
        500L,
        0L,
        200L,
        0L,
        75L,
        250L,
        1.0d,
        false,
        false,
        true,
        4134,
        2,
        577,
        null,
        null,
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
