package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class RobeOfThePurifiedSouls {
  private RobeOfThePurifiedSouls() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.robe_of_the_purified_souls",
        "${item.robe_of_the_purified_souls}",
        BodyPart.BODY,
        "PupWhiteRobe",
        null,
        null,
        "64kInvWhiteRobe",
        3250L,
        5L,
        8.0d,
        25L,
        18L,
        0L,
        0L,
        0L,
        15L,
        103L,
        0.0d,
        false,
        false,
        false,
        41141,
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
            new ItemDefinition.ItemBoost(588, 4, "10", 0, 0),
            new ItemDefinition.ItemBoost(589, 22, "25", 0, 0)),
        List.of(),
        false);
  }
}
