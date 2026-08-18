package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemLargeShield {
  private ItemItemLargeShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.large_shield",
        "${item.large_shield}",
        BodyPart.SHIELD,
        "PupRomanShield",
        null,
        null,
        "64kInvRomanShield",
        8663L,
        10L,
        6.27d,
        17L,
        100L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40368,
        2,
        272,
        null,
        "0",
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
