package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class ConjurerMantle {
  private ConjurerMantle() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.conjurer_mantle",
        "${item.conjurer_mantle}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        5887L,
        5L,
        10.0d,
        25L,
        20L,
        0L,
        0L,
        0L,
        110L,
        20L,
        0.0d,
        false,
        false,
        false,
        41151,
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
            new ItemDefinition.ItemBoost(614, 1, "20", 0, 0),
            new ItemDefinition.ItemBoost(615, 17, "10", 0, 0),
            new ItemDefinition.ItemBoost(616, 18, "10", 0, 0)),
        List.of(),
        false);
  }
}
