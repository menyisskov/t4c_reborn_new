package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MantleOfInfiniteBlessings {
  private MantleOfInfiniteBlessings() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_infinite_blessings",
        "${item.mantle_of_infinite_blessings}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        0L,
        5L,
        20.0d,
        25L,
        30L,
        0L,
        0L,
        0L,
        35L,
        218L,
        0.0d,
        false,
        false,
        false,
        41142,
        2,
        425,
        null,
        "0",
        0,
        -1,
        true,
        null,
        0,
        null,
        0,
        0,
        0,
        List.of(new ItemDefinition.ItemSpell(10302, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(590, 4, "25", 0, 0),
            new ItemDefinition.ItemBoost(591, 12, "5", 0, 0),
            new ItemDefinition.ItemBoost(592, 22, "5", 0, 0),
            new ItemDefinition.ItemBoost(593, 15, "5", 0, 0),
            new ItemDefinition.ItemBoost(594, 13, "5", 0, 0),
            new ItemDefinition.ItemBoost(595, 14, "5", 0, 0),
            new ItemDefinition.ItemBoost(596, 23, "30", 0, 0)),
        List.of(),
        false);
  }
}
