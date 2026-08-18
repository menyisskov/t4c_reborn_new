package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class SapphireBracelet {
  private SapphireBracelet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.sapphire_bracelet",
        "${item.sapphire_bracelet}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        1600L,
        2L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        33L,
        33L,
        0.0d,
        false,
        false,
        false,
        40046,
        2,
        237,
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
            new ItemDefinition.ItemBoost(14, 4, "7", 0, 0),
            new ItemDefinition.ItemBoost(16, 1, "7", 0, 0)),
        List.of(),
        false);
  }
}
