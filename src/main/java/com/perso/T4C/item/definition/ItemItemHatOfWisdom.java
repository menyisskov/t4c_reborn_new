package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHatOfWisdom {
  private ItemItemHatOfWisdom() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hat_of_wisdom",
        "${item.hat_of_wisdom}",
        BodyPart.HEAD,
        "WitchHat5",
        null,
        null,
        "inv_WitchHat5",
        0L,
        0L,
        12.0d,
        10L,
        0L,
        0L,
        0L,
        0L,
        75L,
        300L,
        1.0d,
        false,
        false,
        true,
        3314,
        2,
        878,
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
