package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ArcaneCircletOfPower {
  private ArcaneCircletOfPower() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.arcane_circlet_of_power",
        "${item.arcane_circlet_of_power}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        0L,
        3L,
        0.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        265L,
        15L,
        0.0d,
        false,
        false,
        false,
        41295,
        2,
        279,
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
            new ItemDefinition.ItemBoost(639, 1, "100", 0, 0),
            new ItemDefinition.ItemBoost(640, 23, "-25", 0, 0)),
        List.of(),
        false);
  }
}
