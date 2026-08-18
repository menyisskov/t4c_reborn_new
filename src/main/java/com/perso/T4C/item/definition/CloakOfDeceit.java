package com.perso.T4C.item.definition;

import com.perso.T4C.item.ItemDefinition;
import com.perso.T4C.player.BodyPart;
import java.util.List;

public final class CloakOfDeceit {
  private CloakOfDeceit() {}

  public static ItemDefinition definition() {
    return new ItemDefinition(
        "item.cloak_of_deceit",
        "${item.cloak_of_deceit}",
        BodyPart.BODY,
        "PupNecromanRobe",
        null,
        null,
        "64kInvNecromanRobe",
        1625L,
        5L,
        6.0d,
        25L,
        16L,
        0L,
        0L,
        0L,
        78L,
        15L,
        0.0d,
        false,
        false,
        false,
        41147,
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
            new ItemDefinition.ItemBoost(606, 1, "15", 0, 0),
            new ItemDefinition.ItemBoost(607, 24, "10", 0, 0)),
        List.of(),
        false);
  }
}
