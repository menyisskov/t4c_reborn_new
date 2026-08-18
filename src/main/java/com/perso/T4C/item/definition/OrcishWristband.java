package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class OrcishWristband {
  private OrcishWristband() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.orcish_wristband",
        "${item.orcish_wristband}",
        BodyPart.BRACER,
        null,
        null,
        null,
        "64kInvBracelet",
        5249L,
        2L,
        5.0d,
        0L,
        35L,
        0L,
        0L,
        0L,
        32L,
        38L,
        0.0d,
        false,
        false,
        false,
        40627,
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
            new ItemDefinition.ItemBoost(485, 8, "10", 0, 0),
            new ItemDefinition.ItemBoost(486, 9, "5", 0, 0),
            new ItemDefinition.ItemBoost(881, 10035, "10", 0, 0)),
        List.of(),
        false);
  }
}
