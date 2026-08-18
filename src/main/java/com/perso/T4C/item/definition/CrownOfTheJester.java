package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CrownOfTheJester {
  private CrownOfTheJester() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.crown_of_the_jester",
        "${item.crown_of_the_jester}",
        BodyPart.HEAD,
        "PupGoldenCrown",
        null,
        null,
        "64kInvGoldenCrown",
        736L,
        3L,
        2.0d,
        0L,
        0L,
        0L,
        0L,
        0L,
        19L,
        21L,
        0.0d,
        false,
        false,
        false,
        40277,
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
        List.of(),
        List.of(),
        false);
  }
}
