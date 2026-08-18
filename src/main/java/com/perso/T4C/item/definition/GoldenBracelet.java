package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class GoldenBracelet {
  private GoldenBracelet() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.golden_bracelet",
        "${item.golden_bracelet}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        883L,
        1L,
        1.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        21L,
        24L,
        0.0d,
        false,
        false,
        false,
        40044,
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
            new ItemDefinition.ItemBoost(11, 3, "2", 0, 0),
            new ItemDefinition.ItemBoost(503, 6, "2", 0, 0)),
        List.of(),
        false);
  }
}
