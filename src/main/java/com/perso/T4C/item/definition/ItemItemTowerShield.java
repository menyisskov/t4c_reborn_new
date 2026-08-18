package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemTowerShield {
  private ItemItemTowerShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.tower_shield",
        "${item.tower_shield}",
        BodyPart.SHIELD,
        "PupRomanShield",
        null,
        null,
        "64kInvRomanShield",
        14469L,
        10L,
        8.91d,
        21L,
        125L,
        0L,
        0L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        40249,
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
