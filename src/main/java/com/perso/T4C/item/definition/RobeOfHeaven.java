package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RobeOfHeaven {
  private RobeOfHeaven() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.robe_of_heaven",
        "${item.robe_of_heaven}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        0L,
        5L,
        18.0d,
        25L,
        28L,
        0L,
        0L,
        0L,
        73L,
        135L,
        0.0d,
        false,
        false,
        false,
        41467,
        2,
        425,
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
            new ItemDefinition.ItemBoost(887, 4, "25", 0, 0),
            new ItemDefinition.ItemBoost(888, 13, "10", 0, 0),
            new ItemDefinition.ItemBoost(889, 14, "10", 0, 0),
            new ItemDefinition.ItemBoost(890, 12, "10", 0, 0),
            new ItemDefinition.ItemBoost(891, 15, "10", 0, 0),
            new ItemDefinition.ItemBoost(892, 22, "10", 0, 0)),
        List.of(),
        false);
  }
}
