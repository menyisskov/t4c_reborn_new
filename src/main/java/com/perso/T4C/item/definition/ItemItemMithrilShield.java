package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemMithrilShield {
  private ItemItemMithrilShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mithril_shield",
        "${item.mithril_shield}",
        BodyPart.SHIELD,
        "PupCentaurShield2",
        null,
        null,
        "64kInvCentaurShield2",
        21761L,
        11L,
        12.21d,
        25L,
        150L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40369,
        2,
        461,
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
