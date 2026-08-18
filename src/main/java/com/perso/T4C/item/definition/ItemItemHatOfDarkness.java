package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemHatOfDarkness {
  private ItemItemHatOfDarkness() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.hat_of_darkness",
        "${item.hat_of_darkness}",
        BodyPart.HEAD,
        "WitchHat1",
        null,
        null,
        "inv_WitchHat1",
        0L,
        0L,
        12.0d,
        10L,
        0L,
        0L,
        0L,
        0L,
        300L,
        75L,
        1.0d,
        false,
        false,
        true,
        3467,
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
