package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemWindShield {
  private ItemItemWindShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.wind_shield",
        "${item.wind_shield}",
        BodyPart.SHIELD,
        null,
        null,
        null,
        "InvDiamondFocus",
        500000L,
        2L,
        35.0d,
        0L,
        300L,
        0L,
        0L,
        0L,
        125L,
        350L,
        1.0d,
        false,
        false,
        true,
        3512,
        2,
        569,
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
