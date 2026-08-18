package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class MantleOfDeath {
  private MantleOfDeath() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.mantle_of_death",
        "${item.mantle_of_death}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        0L,
        5L,
        15.0d,
        25L,
        25L,
        0L,
        0L,
        0L,
        201L,
        15L,
        0.0d,
        false,
        false,
        false,
        41149,
        2,
        278,
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
        List.of(new ItemDefinition.ItemSpell(10304, 0, 100)),
        List.of(
            new ItemDefinition.ItemBoost(610, 1, "40", 0, 0),
            new ItemDefinition.ItemBoost(611, 24, "20", 0, 0)),
        List.of(),
        false);
  }
}
