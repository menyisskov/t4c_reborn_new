package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class DarkwoodShield {
  private DarkwoodShield() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.darkwood_shield",
        "${item.darkwood_shield}",
        BodyPart.SHIELD,
        "PupSkavenShield3",
        null,
        null,
        "64kInvSkavenShield3",
        0L,
        10L,
        20.0d,
        0L,
        180L,
        500L,
        150L,
        0L,
        0L,
        0L,
        0.0d,
        false,
        false,
        false,
        41717,
        2,
        467,
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
            new ItemDefinition.ItemBoost(1007, 14, "10", 0, 0),
            new ItemDefinition.ItemBoost(1008, 13, "-10", 0, 0),
            new ItemDefinition.ItemBoost(1009, 10001, "10", 0, 0),
            new ItemDefinition.ItemBoost(1010, 10002, "25", 0, 0)),
        List.of(),
        false);
  }
}
