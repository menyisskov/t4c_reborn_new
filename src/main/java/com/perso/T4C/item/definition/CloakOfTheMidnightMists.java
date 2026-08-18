package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CloakOfTheMidnightMists {
  private CloakOfTheMidnightMists() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_the_midnight_mists",
        "${item.cloak_of_the_midnight_mists}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        0L,
        5L,
        25.0d,
        25L,
        35L,
        0L,
        0L,
        0L,
        265L,
        15L,
        0.0d,
        false,
        false,
        false,
        41150,
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
            new ItemDefinition.ItemBoost(612, 1, "50", 0, 0),
            new ItemDefinition.ItemBoost(613, 24, "50", 0, 0)),
        List.of(),
        false);
  }
}
