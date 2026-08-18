package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class Drachenplate {
  private Drachenplate() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.drachenplate",
        "${item.drachenplate}",
        BodyPart.BODY,
        "PupChainMailBody",
        null,
        null,
        "64kInvScale",
        0L,
        5L,
        16.75d,
        34L,
        185L,
        0L,
        0L,
        0L,
        43L,
        43L,
        0.0d,
        false,
        false,
        false,
        41665,
        2,
        188,
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
            new ItemDefinition.ItemBoost(949, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(950, 22, "10", 0, 0),
            new ItemDefinition.ItemBoost(951, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(952, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(953, 14, "10", 0, 0)),
        List.of(),
        false);
  }
}
