package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CloakOfTheArcher {
  private CloakOfTheArcher() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_the_archer",
        "${item.cloak_of_the_archer}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        37671L,
        7L,
        10.0d,
        0L,
        0L,
        0L,
        25L,
        70L,
        30L,
        40L,
        0.0d,
        false,
        false,
        false,
        41397,
        2,
        278,
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
            new ItemDefinition.ItemBoost(780, 10035, "50", 0, 0),
            new ItemDefinition.ItemBoost(781, 9, "25", 0, 0)),
        List.of(),
        false);
  }
}
