package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class BootsOfSpeed {
  private BootsOfSpeed() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.boots_of_speed",
        "${item.boots_of_speed}",
        BodyPart.FEET,
        "PupLeatherBoots",
        null,
        null,
        "64kIconBoots",
        310L,
        3L,
        1.0d,
        0L,
        25L,
        0L,
        0L,
        0L,
        20L,
        20L,
        0.0d,
        false,
        false,
        false,
        40102,
        2,
        213,
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
        List.of(
            new ItemDefinition.ItemBoost(53, 6, "10", 25, 10),
            new ItemDefinition.ItemBoost(54, 9, "10", 20, 10)),
        List.of(),
        false);
  }
}
