package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ItemItemCapeOfFireResistance {
  private ItemItemCapeOfFireResistance() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cape_of_fire_resistance",
        "${item.cape_of_fire_resistance}",
        BodyPart.BACK,
        "PupRedCape",
        null,
        null,
        "64kInvRedCape",
        1625L,
        4L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        45L,
        35L,
        0.0d,
        false,
        false,
        false,
        40628,
        2,
        287,
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
        List.of(new ItemDefinition.ItemBoost(466, 13, "20", 0, 0)),
        List.of(),
        false);
  }
}
